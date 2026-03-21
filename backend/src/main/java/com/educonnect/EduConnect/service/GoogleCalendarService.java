package com.educonnect.EduConnect.service;

import com.educonnect.EduConnect.config.GoogleCalendarProperties;
import com.educonnect.EduConnect.dto.GoogleCalendarSyncResponseDTO;
import com.educonnect.EduConnect.dto.GoogleCalendarStatusDTO;
import com.educonnect.EduConnect.model.Evento;
import com.educonnect.EduConnect.model.GoogleCalendarCredential;
import com.educonnect.EduConnect.model.GoogleCalendarOAuthState;
import com.educonnect.EduConnect.model.Usuario;
import com.educonnect.EduConnect.model.enums.Role;
import com.educonnect.EduConnect.repository.EventoRepository;
import com.educonnect.EduConnect.repository.GoogleCalendarCredentialRepository;
import com.educonnect.EduConnect.repository.GoogleCalendarOAuthStateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleCalendarService {

    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_CALENDAR_API_BASE = "https://www.googleapis.com/calendar/v3/calendars";

    private final GoogleCalendarCredentialRepository credentialRepository;
    private final GoogleCalendarOAuthStateRepository oauthStateRepository;
    private final EventoRepository eventoRepository;
    private final GoogleCalendarProperties properties;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Transactional(readOnly = true)
    public GoogleCalendarStatusDTO getStatus(Usuario usuario) {
        validarPermissao(usuario);

        GoogleCalendarStatusDTO dto = new GoogleCalendarStatusDTO();
        dto.setConfigured(properties.isConfigured());
        dto.setEventosCriados(eventoRepository.countByCriador_Id(usuario.getId()));
        dto.setEventosSincronizados(eventoRepository.countByCriador_IdAndGoogleCalendarEventIdIsNotNullAndGoogleCalendarSyncErrorIsNull(usuario.getId()));

        if (properties.isConfigured()) {
            credentialRepository.findByUsuario_Id(usuario.getId()).ifPresent(credential -> {
                dto.setConnected(true);
                dto.setCalendarId(credential.getCalendarId());
                dto.setConnectedAt(credential.getConnectedAt());
            });
        }

        return dto;
    }

    @Transactional
    public String criarUrlAutorizacao(Usuario usuario) {
        validarPermissao(usuario);
        validarConfiguracao();

        oauthStateRepository.deleteByExpiresAtBefore(LocalDateTime.now());

        GoogleCalendarOAuthState state = new GoogleCalendarOAuthState();
        state.setStateValue(UUID.randomUUID().toString());
        state.setUsuario(usuario);
        state.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        oauthStateRepository.save(state);

        return UriComponentsBuilder.fromUriString(GOOGLE_AUTH_URL)
            .queryParam("client_id", properties.getClientId())
            .queryParam("redirect_uri", properties.getRedirectUri())
            .queryParam("response_type", "code")
            .queryParam("scope", properties.getScope())
            .queryParam("access_type", "offline")
            .queryParam("prompt", "consent")
            .queryParam("include_granted_scopes", "true")
            .queryParam("state", state.getStateValue())
            .build(true)
            .toUriString();
    }

    @Transactional
    public URI processarCallback(String code, String stateValue, String error) {
        oauthStateRepository.deleteByExpiresAtBefore(LocalDateTime.now());

        if (!StringUtils.hasText(stateValue)) {
            return construirRedirectFrontend("error", "Estado da autenticação não foi informado.", 0, 0);
        }

        Optional<GoogleCalendarOAuthState> stateOpt = oauthStateRepository.findByStateValue(stateValue);
        if (stateOpt.isEmpty()) {
            return construirRedirectFrontend("error", "Estado da autenticação expirou ou é inválido.", 0, 0);
        }

        GoogleCalendarOAuthState state = stateOpt.get();
        oauthStateRepository.delete(state);

        if (state.getExpiresAt().isBefore(LocalDateTime.now())) {
            return construirRedirectFrontend("error", "A autenticação com o Google expirou. Tente novamente.", 0, 0);
        }

        if (!properties.isConfigured()) {
            return construirRedirectFrontend("error", "A integração com Google Calendar não está configurada.", 0, 0);
        }

        if (StringUtils.hasText(error)) {
            return construirRedirectFrontend("error", "O Google recusou a autorização: " + error, 0, 0);
        }

        if (!StringUtils.hasText(code)) {
            return construirRedirectFrontend("error", "O Google não retornou um código de autorização válido.", 0, 0);
        }

        try {
            TokenPayload tokenPayload = trocarCodePorTokens(code);
            salvarCredencial(state.getUsuario(), tokenPayload);
            limparVinculosGoogleDosEventos(state.getUsuario());
            GoogleCalendarSyncResponseDTO sync = sincronizarEventosDoCriador(state.getUsuario(), true);
            return construirRedirectFrontend("connected", sync.getMensagem(), sync.getSincronizados(), sync.getFalhas());
        } catch (Exception ex) {
            return construirRedirectFrontend("error", ex.getMessage(), 0, 0);
        }
    }

    @Transactional
    public void desconectar(Usuario usuario) {
        validarPermissao(usuario);
        credentialRepository.deleteByUsuario_Id(usuario.getId());
        limparVinculosGoogleDosEventos(usuario);
    }

    @Transactional
    public GoogleCalendarSyncResponseDTO sincronizarEventosDoCriador(Usuario usuario) {
        validarPermissao(usuario);
        validarConfiguracao();
        return sincronizarEventosDoCriador(usuario, false);
    }

    @Transactional
    public void sincronizarEventoAposSalvar(Evento evento) {
        if (evento == null || evento.getCriador() == null || !properties.isConfigured()) {
            return;
        }

        Optional<GoogleCalendarCredential> credentialOpt = credentialRepository.findByUsuario_Id(evento.getCriador().getId());
        if (credentialOpt.isEmpty()) {
            if (StringUtils.hasText(evento.getGoogleCalendarEventId())) {
                evento.setGoogleCalendarSyncError("A conta Google Calendar do criador não está conectada.");
                eventoRepository.save(evento);
            }
            return;
        }

        try {
            SyncOutcome outcome = criarOuAtualizarEventoNoGoogle(evento, credentialOpt.get());
            aplicarResultadoDaSincronizacao(evento, outcome);
            eventoRepository.save(evento);
        } catch (Exception ex) {
            evento.setGoogleCalendarSyncError(ex.getMessage());
            eventoRepository.save(evento);
        }
    }

    @Transactional
    public void deletarEventoNoGoogleSeNecessario(Evento evento) {
        if (evento == null || !StringUtils.hasText(evento.getGoogleCalendarEventId()) || evento.getCriador() == null || !properties.isConfigured()) {
            return;
        }

        Optional<GoogleCalendarCredential> credentialOpt = credentialRepository.findByUsuario_Id(evento.getCriador().getId());
        if (credentialOpt.isEmpty()) {
            return;
        }

        try {
            GoogleCalendarCredential credential = garantirAccessTokenValido(credentialOpt.get());
            executarRequestSemCorpo(
                "DELETE",
                montarUrlEvento(credential.getCalendarId(), evento.getGoogleCalendarEventId()),
                credential.getAccessToken()
            );
        } catch (Exception ignored) {
            // Não bloqueamos a exclusão local por falha remota.
        }
    }

    private GoogleCalendarSyncResponseDTO sincronizarEventosDoCriador(Usuario usuario, boolean chamadaInterna) {
        if (!chamadaInterna) {
            validarPermissao(usuario);
            validarConfiguracao();
        }

        GoogleCalendarCredential credential = credentialRepository.findByUsuario_Id(usuario.getId())
            .orElseThrow(() -> new RuntimeException("Conecte sua conta Google Calendar antes de sincronizar."));

        List<Evento> eventos = eventoRepository.findByCriador_IdOrderByDataInicioAsc(usuario.getId());

        int sincronizados = 0;
        int falhas = 0;

        for (Evento evento : eventos) {
            try {
                SyncOutcome outcome = criarOuAtualizarEventoNoGoogle(evento, credential);
                aplicarResultadoDaSincronizacao(evento, outcome);
                if (outcome.success()) {
                    sincronizados++;
                } else {
                    falhas++;
                }
            } catch (Exception ex) {
                evento.setGoogleCalendarSyncError(ex.getMessage());
                falhas++;
            }
            eventoRepository.save(evento);
        }

        String mensagem = "Nenhum evento proprio encontrado para sincronizar.";
        if (!eventos.isEmpty()) {
            mensagem = sincronizados + " evento(s) sincronizado(s) e " + falhas + " falha(s).";
        }

        return new GoogleCalendarSyncResponseDTO(eventos.size(), sincronizados, falhas, mensagem);
    }

    private SyncOutcome criarOuAtualizarEventoNoGoogle(Evento evento, GoogleCalendarCredential credential) throws IOException, InterruptedException {
        GoogleCalendarCredential credentialAtualizada = garantirAccessTokenValido(credential);
        Map<String, Object> payload = construirPayloadEvento(evento);
        String urlBase = montarUrlCalendario(credentialAtualizada.getCalendarId()) + "?sendUpdates=none";

        JsonNode responseNode;
        if (StringUtils.hasText(evento.getGoogleCalendarEventId())) {
            responseNode = executarRequestJson(
                "PUT",
                montarUrlEvento(credentialAtualizada.getCalendarId(), evento.getGoogleCalendarEventId()) + "?sendUpdates=none",
                credentialAtualizada.getAccessToken(),
                payload
            );
        } else {
            responseNode = executarRequestJson("POST", urlBase, credentialAtualizada.getAccessToken(), payload);
        }

        String externalEventId = responseNode.path("id").asText(null);
        if (!StringUtils.hasText(externalEventId)) {
            throw new RuntimeException("O Google Calendar não retornou o identificador do evento sincronizado.");
        }

        return new SyncOutcome(true, externalEventId, LocalDateTime.now(), null);
    }

    private Map<String, Object> construirPayloadEvento(Evento evento) {
        ZonedDateTime inicio = evento.getDataInicio().atZone(resolverTimeZone());
        ZonedDateTime fim = evento.getDataFim() != null
            ? evento.getDataFim().atZone(resolverTimeZone())
            : inicio.plusMinutes(Math.max(properties.getDefaultDurationMinutes(), 1));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("summary", evento.getTitulo());
        payload.put("description", montarDescricaoEvento(evento));

        Map<String, Object> start = new LinkedHashMap<>();
        start.put("dateTime", inicio.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        start.put("timeZone", resolverTimeZone().getId());
        payload.put("start", start);

        Map<String, Object> end = new LinkedHashMap<>();
        end.put("dateTime", fim.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        end.put("timeZone", resolverTimeZone().getId());
        payload.put("end", end);

        Map<String, Object> privateProps = new LinkedHashMap<>();
        privateProps.put("educonnectEventId", String.valueOf(evento.getId()));
        privateProps.put("educonnectTipo", evento.getTipo());

        Map<String, Object> extendedProperties = new LinkedHashMap<>();
        extendedProperties.put("private", privateProps);
        payload.put("extendedProperties", extendedProperties);

        return payload;
    }

    private String montarDescricaoEvento(Evento evento) {
        StringBuilder descricao = new StringBuilder();
        if (StringUtils.hasText(evento.getDescricao())) {
            descricao.append(evento.getDescricao().trim()).append("\n\n");
        }
        descricao.append("Sincronizado automaticamente pelo EduConnect.");
        descricao.append("\nTipo: ").append(evento.getTipo());
        descricao.append("\nID interno: ").append(evento.getId());
        return descricao.toString();
    }

    private GoogleCalendarCredential garantirAccessTokenValido(GoogleCalendarCredential credential) throws IOException, InterruptedException {
        if (credential.getAccessTokenExpiresAt() == null || credential.getAccessTokenExpiresAt().isAfter(LocalDateTime.now().plusMinutes(1))) {
            return credential;
        }

        if (!StringUtils.hasText(credential.getRefreshToken())) {
            throw new RuntimeException("A conexão com Google Calendar expirou. Conecte novamente sua conta.");
        }

        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", properties.getClientId());
        params.add("client_secret", properties.getClientSecret());
        params.add("refresh_token", credential.getRefreshToken());
        params.add("grant_type", "refresh_token");

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(GOOGLE_TOKEN_URL))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(formEncode(params.toSingleValueMap())))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode body = parseJson(response.body());

        if (response.statusCode() >= 400) {
            credentialRepository.deleteByUsuario_Id(credential.getUsuario().getId());
            throw new RuntimeException("A conexão com Google Calendar expirou ou foi revogada. Conecte novamente sua conta.");
        }

        credential.setAccessToken(body.path("access_token").asText());
        credential.setAccessTokenExpiresAt(LocalDateTime.now().plusSeconds(Math.max(body.path("expires_in").asLong(3600) - 60, 60)));
        return credentialRepository.save(credential);
    }

    private TokenPayload trocarCodePorTokens(String code) throws IOException, InterruptedException {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", properties.getClientId());
        params.add("client_secret", properties.getClientSecret());
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", properties.getRedirectUri());

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(GOOGLE_TOKEN_URL))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(formEncode(params.toSingleValueMap())))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode body = parseJson(response.body());

        if (response.statusCode() >= 400) {
            throw new RuntimeException(extrairMensagemErroGoogle(body, "Não foi possível conectar com Google Calendar."));
        }

        String accessToken = body.path("access_token").asText(null);
        String refreshToken = body.path("refresh_token").asText(null);

        if (!StringUtils.hasText(accessToken) || !StringUtils.hasText(refreshToken)) {
            throw new RuntimeException("O Google não retornou todos os tokens necessários para sincronizar o calendário.");
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(Math.max(body.path("expires_in").asLong(3600) - 60, 60));
        return new TokenPayload(accessToken, refreshToken, expiresAt);
    }

    private void salvarCredencial(Usuario usuario, TokenPayload tokenPayload) {
        credentialRepository.deleteByUsuario_Id(usuario.getId());

        GoogleCalendarCredential credential = new GoogleCalendarCredential();

        credential.setUsuario(usuario);
        credential.setCalendarId(properties.getDefaultCalendarId());
        credential.setAccessToken(tokenPayload.accessToken());
        credential.setRefreshToken(tokenPayload.refreshToken());
        credential.setAccessTokenExpiresAt(tokenPayload.expiresAt());
        credentialRepository.save(credential);
    }

    private void limparVinculosGoogleDosEventos(Usuario usuario) {
        List<Evento> eventos = eventoRepository.findByCriador_IdOrderByDataInicioAsc(usuario.getId());
        for (Evento evento : eventos) {
            evento.setGoogleCalendarEventId(null);
            evento.setGoogleCalendarLastSyncedAt(null);
            evento.setGoogleCalendarSyncError(null);
        }
        eventoRepository.saveAll(eventos);
    }

    private void aplicarResultadoDaSincronizacao(Evento evento, SyncOutcome outcome) {
        if (outcome.success()) {
            evento.setGoogleCalendarEventId(outcome.externalEventId());
            evento.setGoogleCalendarLastSyncedAt(outcome.syncedAt());
            evento.setGoogleCalendarSyncError(null);
            return;
        }
        evento.setGoogleCalendarSyncError(outcome.errorMessage());
    }

    private JsonNode executarRequestJson(String method, String url, String accessToken, Map<String, Object> payload) throws IOException, InterruptedException {
        String requestBody = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + accessToken)
            .header("Content-Type", "application/json")
            .method(method, HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode body = parseJson(response.body());

        if (response.statusCode() >= 400) {
            throw new RuntimeException(extrairMensagemErroGoogle(body, "Falha ao sincronizar evento com Google Calendar."));
        }

        return body;
    }

    private void executarRequestSemCorpo(String method, String url, String accessToken) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + accessToken)
            .method(method, HttpRequest.BodyPublishers.noBody())
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 404) {
            return;
        }
        if (response.statusCode() >= 400) {
            JsonNode body = parseJson(response.body());
            throw new RuntimeException(extrairMensagemErroGoogle(body, "Falha ao remover evento do Google Calendar."));
        }
    }

    private JsonNode parseJson(String content) throws IOException {
        if (!StringUtils.hasText(content)) {
            return objectMapper.createObjectNode();
        }
        return objectMapper.readTree(content);
    }

    private String extrairMensagemErroGoogle(JsonNode body, String fallback) {
        JsonNode errorNode = body.path("error");
        if (errorNode.isTextual() && StringUtils.hasText(errorNode.asText())) {
            return errorNode.asText();
        }
        if (errorNode.has("message") && StringUtils.hasText(errorNode.path("message").asText())) {
            return errorNode.path("message").asText();
        }
        if (body.has("message") && StringUtils.hasText(body.path("message").asText())) {
            return body.path("message").asText();
        }
        return fallback;
    }

    private URI construirRedirectFrontend(String status, String message, int synced, int failed) {
        String frontendRedirectUrl = StringUtils.hasText(properties.getFrontendRedirectUrl())
            ? properties.getFrontendRedirectUrl()
            : "http://localhost:3000/calendario";

        return UriComponentsBuilder.fromUriString(frontendRedirectUrl)
            .queryParam("googleCalendar", status)
            .queryParam("googleCalendarMessage", message)
            .queryParam("googleCalendarSynced", synced)
            .queryParam("googleCalendarFailed", failed)
            .build(true)
            .toUri();
    }

    private String montarUrlCalendario(String calendarId) {
        return GOOGLE_CALENDAR_API_BASE + "/" + urlEncode(calendarId) + "/events";
    }

    private String montarUrlEvento(String calendarId, String eventId) {
        return GOOGLE_CALENDAR_API_BASE + "/" + urlEncode(calendarId) + "/events/" + urlEncode(eventId);
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String formEncode(Map<String, String> params) {
        StringBuilder body = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (body.length() > 0) {
                body.append('&');
            }
            body.append(urlEncode(entry.getKey()))
                .append('=')
                .append(urlEncode(entry.getValue()));
        }
        return body.toString();
    }

    private void validarConfiguracao() {
        if (!properties.isConfigured()) {
            throw new RuntimeException("A integração com Google Calendar não está configurada no ambiente.");
        }
    }

    private void validarPermissao(Usuario usuario) {
        Role role = usuario.getRole();
        if (role != Role.ADMINISTRADOR && role != Role.DIRETORIA && role != Role.PROFESSOR) {
            throw new RuntimeException("Somente professores, diretoria e administradores podem usar a integração com Google Calendar.");
        }
    }

    private ZoneId resolverTimeZone() {
        return ZoneId.of(properties.getTimeZone());
    }

    private record TokenPayload(String accessToken, String refreshToken, LocalDateTime expiresAt) {
    }

    private record SyncOutcome(boolean success, String externalEventId, LocalDateTime syncedAt, String errorMessage) {
    }
}
