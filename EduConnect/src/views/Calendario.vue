<template>
  <div class="container-fluid py-4">
    <div class="row mb-4">
      <div class="col">
        <h2 class="fw-bold">
          <i class="bi bi-calendar-event-fill me-2"></i>Calendário Escolar
        </h2>
        <p class="text-muted">Confira provas, eventos e compromissos</p>
      </div>
    </div>
    
    <div class="row">
      <div class="col-lg-9">
        <div class="card shadow-sm">
          <div class="card-body">
            <vue-cal 
              :events="eventos"
              :time="false"
              locale="pt-br"
              :disable-views="['years', 'year']"
              default-view="month"
              @cell-click="handleCellClick"
              @event-click="handleEventClick"
            />
          </div>
        </div>
      </div>
      
      <div class="col-lg-3">
        <div class="card shadow-sm mb-3">
          <div class="card-header bg-white">
            <h6 class="mb-0">Legenda</h6>
          </div>
          <div class="card-body">
            <div class="mb-2">
              <span class="badge bg-primary me-2">●</span>Prova
            </div>
            <div class="mb-2">
              <span class="badge bg-success me-2">●</span>Evento
            </div>
            <div class="mb-2">
              <span class="badge bg-warning me-2">●</span>Reunião
            </div>
            <div class="mb-2">
              <span class="badge bg-info me-2">●</span>Feriado
            </div>
          </div>
        </div>
        
        <div class="card shadow-sm">
          <div class="card-header bg-white">
            <h6 class="mb-0">Próximos Eventos</h6>
          </div>
          <div class="card-body">
            <div v-for="evento in proximosEventos" :key="evento.id" class="evento-item mb-3">
              <div class="d-flex">
                <div class="evento-date me-3">
                  <div class="day">{{ evento.dia }}</div>
                  <div class="month">{{ evento.mes }}</div>
                </div>
                <div>
                  <h6 class="mb-0">{{ evento.titulo }}</h6>
                  <small class="text-muted">{{ evento.horario }}</small>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import VueCal from 'vue-cal'
import 'vue-cal/dist/vuecal.css'

const eventos = ref([
  {
    start: '2025-10-01 14:00',
    end: '2025-10-01 16:00',
    title: 'Prova de Matemática',
    class: 'bg-primary'
  },
  {
    start: '2025-10-05 09:00',
    end: '2025-10-05 12:00',
    title: 'Feira de Ciências',
    class: 'bg-success'
  }
])

const proximosEventos = ref([
  { id: 1, titulo: 'Prova de Matemática', dia: '01', mes: 'OUT', horario: '14:00' },
  { id: 2, titulo: 'Feira de Ciências', dia: '05', mes: 'OUT', horario: '09:00' }
])

const handleCellClick = (date) => {
  console.log('Data clicada:', date)
}

const handleEventClick = (evento) => {
  console.log('Evento clicado:', evento)
}
</script>

<style scoped>
.evento-date {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0.5rem;
  border-radius: 8px;
  text-align: center;
  min-width: 50px;
}
.evento-date .day { font-size: 1.25rem; font-weight: bold; line-height: 1; }
.evento-date .month { font-size: 0.7rem; text-transform: uppercase; }
</style>

