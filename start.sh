#!/bin/bash

# Script de inicialização do EduConnect com Docker
# Autor: EduConnect Team
# Uso: ./start.sh

set -e

echo "🎓 EduConnect - Iniciando Sistema com Docker"
echo "=============================================="
echo ""

# Verificar se Docker está instalado
if ! command -v docker &> /dev/null; then
    echo "❌ Docker não encontrado. Por favor, instale o Docker primeiro."
    exit 1
fi

# Verificar se Docker Compose está instalado
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose não encontrado. Por favor, instale o Docker Compose primeiro."
    exit 1
fi

# Verificar se portas estão livres
check_port() {
    if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        echo "⚠️  Porta $1 está em uso. Por favor, libere a porta ou altere no docker-compose.yml"
        return 1
    fi
    return 0
}

echo "🔍 Verificando portas..."
check_port 3000 || exit 1
check_port 4306 || exit 1
check_port 8080 || exit 1

echo "✅ Todas as portas estão livres"
echo ""

# Parar containers existentes (se houver)
echo "🛑 Parando containers existentes..."
docker-compose down 2>/dev/null || true
echo ""

# Build e start
echo "🏗️  Construindo e iniciando containers..."
echo "   Isso pode levar alguns minutos na primeira vez..."
echo ""
docker-compose up -d --build

echo ""
echo "⏳ Aguardando serviços iniciarem..."
echo ""

# Aguardar MySQL
echo "📊 Aguardando MySQL..."
sleep 10
until docker exec educonnect-mysql mysqladmin ping -h localhost --silent 2>/dev/null; do
    echo "   MySQL ainda não está pronto... aguardando"
    sleep 2
done
echo "✅ MySQL pronto!"

# Aguardar Backend
echo "🔧 Aguardando Backend..."
sleep 20
until curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; do
    echo "   Backend ainda não está pronto... aguardando"
    sleep 5
done
echo "✅ Backend pronto!"

# Aguardar Frontend
echo "🎨 Aguardando Frontend..."
sleep 5
until curl -f http://localhost:3000 > /dev/null 2>&1; do
    echo "   Frontend ainda não está pronto... aguardando"
    sleep 2
done
echo "✅ Frontend pronto!"

echo ""
echo "=============================================="
echo "🎉 EduConnect iniciado com sucesso!"
echo "=============================================="
echo ""
echo "📱 Acesse a aplicação:"
echo "   Frontend: http://localhost:3000"
echo "   Backend:  http://localhost:8080"
echo "   MySQL:    localhost:4306"
echo ""
echo "🔐 Credenciais de teste:"
echo "   Admin:       admin@educonnect.com / admin123"
echo "   Professor:   professor@educonnect.com / prof123"
echo "   Aluno:       aluno@educonnect.com / aluno123"
echo "   Responsável: responsavel@educonnect.com / resp123"
echo ""
echo "📊 Comandos úteis:"
echo "   Ver logs:        docker-compose logs -f"
echo "   Parar tudo:      docker-compose down"
echo "   Reiniciar:       docker-compose restart"
echo "   Status:          docker-compose ps"
echo ""
echo "📖 Documentação completa: DOCKER-README.md"
echo ""

