# 🍰 Bons Momentos — Sistema de Confeitaria & Gestão

Sistema completo desenvolvido para a confeitaria **Bons Momentos**, composto por catálogo virtual, painel administrativo, controle de pedidos em tempo real, painel da cozinha (Kanban) e aplicativo mobile para entregadores.

## 🚀 Tecnologias Utilizadas

* **Backend / Banco de Dados**: Supabase (PostgreSQL)
* **Aplicativo Mobile (Entregador)**: Kotlin, Android SDK, ViewBinding, Room Database, Retrofit, Coroutines

---

## 📱 Módulo Mobile (App do Entregador)

O aplicativo dos entregadores permite o login seguro por **CPF e senha**, listagem de pedidos designados (`Saiu para entrega`).

### ⚙️ Configuração e Execução para Desenvolvimento

Para rodar o projeto na sua máquina, é necessário configurar as credenciais do seu próprio banco de dados Supabase de forma segura através do arquivo `local.properties` (que é ignorado pelo Git e nunca será exposto no repositório).

1. **Clone o repositório:**
   ```bash
   git clone [https://github.com/reinaldo-ss/Projeto-PIM-BIM2-2026.git](https://github.com/reinaldo-ss/Projeto-PIM-BIM2-2026.git)
