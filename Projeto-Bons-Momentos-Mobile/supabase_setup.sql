-- =====================================================================
--  Bons Momentos — preparação do Supabase para o APP DO ENTREGADOR  (v2)
--  Conferido contra a estrutura REAL do banco (ids "integer", status do
--  grupo: 'a fazer', 'Em Preparo', 'Saiu para entrega').
--
--  Como usar: Supabase > SQL Editor > New query > cole TUDO > Run.
--  PARTES 1 e 2 NÃO apagam nem alteram dados existentes (só acrescentam).
--  PARTES 3 e 4 estão comentadas de propósito (veja as instruções).
-- =====================================================================

-- ---------------------------------------------------------------------
-- PARTE 1 — Colunas novas (se já existirem, são ignoradas)
-- ---------------------------------------------------------------------
-- Qual motoboy entrega o pedido (a Cozinha preenche ao clicar em "Enviar").
-- Observação: no seu banco esta coluna JÁ EXISTE, então esta linha não faz nada.
alter table public.pedidos
  add column if not exists id_motoboy integer references public.motoboy(id);

-- Login do entregador (Supabase Auth): email + vínculo com a conta de login
alter table public.motoboy add column if not exists email varchar;
alter table public.motoboy
  add column if not exists auth_id uuid references auth.users(id) on delete set null;

create unique index if not exists motoboy_email_uniq   on public.motoboy (lower(email));
create unique index if not exists motoboy_auth_id_uniq on public.motoboy (auth_id);
create index        if not exists pedidos_motoboy_idx  on public.pedidos (id_motoboy);

-- ---------------------------------------------------------------------
-- PARTE 2 — Funções que o app usa (o app NÃO lê as tabelas diretamente)
--   * cada entregador só enxerga/conclui os PRÓPRIOS pedidos
--   * o app nunca tem acesso às colunas "senha"
-- ---------------------------------------------------------------------
create or replace function public.meu_motoboy_id()
returns integer
language sql stable security definer set search_path = public
as $$
  select m.id from public.motoboy m where m.auth_id = auth.uid()
$$;

create or replace function public.meu_perfil()
returns table (id integer, nome text)
language sql stable security definer set search_path = public
as $$
  select m.id, m.nome::text from public.motoboy m where m.auth_id = auth.uid()
$$;

-- Pedidos "disponíveis" do entregador = os que a cozinha enviou a ele
-- (id_motoboy dele + status 'Saiu para entrega', sem diferenciar maiúsculas).
create or replace function public.meus_pedidos()
returns table (id integer, produto text, pagamento text, cep text, endereco text, status text)
language sql stable security definer set search_path = public
as $$
  select p.id,
         r.nome::text,
         p.opcao_pagamento::text,
         coalesce(substring(p.endereco_entrega from '[0-9]{5}-?[0-9]{3}'), u.cep::text, '') as cep,
         p.endereco_entrega::text,
         p.status::text
  from public.pedidos p
  join public.receitas r on r.id = p.id_receita
  join public.usuarios u on u.id = p.id_usuario
  where p.id_motoboy = public.meu_motoboy_id()
    and lower(btrim(p.status)) = 'saiu para entrega'
  order by p.id
$$;

-- Botão "Concluir": só funciona para pedido que é DESTE entregador.
-- Pode ser chamado de novo sem problema (o app reenvia se a internet cair).
create or replace function public.concluir_pedido(p_id integer)
returns void
language plpgsql security definer set search_path = public
as $$
begin
  update public.pedidos
     set status = 'Entregue'
   where id = p_id
     and id_motoboy = public.meu_motoboy_id()
     and lower(btrim(status)) in ('saiu para entrega', 'entregue');
  if not found then
    raise exception 'Pedido % não pertence a este entregador', p_id;
  end if;
end
$$;

-- Só usuários logados (authenticated) podem chamar as funções
revoke all on function public.meu_motoboy_id()        from public, anon;
revoke all on function public.meu_perfil()            from public, anon;
revoke all on function public.meus_pedidos()          from public, anon;
revoke all on function public.concluir_pedido(integer) from public, anon;
grant execute on function public.meu_motoboy_id()        to authenticated;
grant execute on function public.meu_perfil()            to authenticated;
grant execute on function public.meus_pedidos()          to authenticated;
grant execute on function public.concluir_pedido(integer) to authenticated;

notify pgrst, 'reload schema';

-- ---------------------------------------------------------------------
-- PARTE 3 — SEGURANÇA (RLS)  >>> LEIA ANTES DE USAR <<<
-- CONFERIDO no seu banco: a RLS está DESLIGADA nas 4 tabelas e não há
-- nenhuma policy. Ou seja, qualquer pessoa com a chave pública do app
-- consegue ler (e alterar) usuarios, motoboy, pedidos e receitas,
-- INCLUSIVE as colunas "senha". Isso vale para o site também.
-- Ligar a RLS fecha esse acesso direto; as funções acima continuam
-- funcionando. PORÉM, se o site/back-end da equipe lê ou grava essas
-- tabelas com a chave pública, ele vai parar de funcionar. Combine com a
-- equipe e só então remova os "-- " das linhas abaixo.
-- ---------------------------------------------------------------------
-- alter table public.usuarios enable row level security;
-- alter table public.pedidos  enable row level security;
-- alter table public.motoboy  enable row level security;
-- alter table public.receitas enable row level security;
-- create policy "receitas visiveis a todos" on public.receitas
--   for select to anon, authenticated using (true);

-- ---------------------------------------------------------------------
-- PARTE 4 — DADOS DE TESTE (rode DEPOIS de criar o usuário em
-- Authentication > Users > Add user, com "Auto Confirm User" marcado).
-- Selecione as linhas do bloco e aperte Ctrl+/ para tirar os "--".
-- Só troque o EMAIL (pelo mesmo que você criou no Authentication).
-- Rode UM bloco por vez. Eles usam o motoboy de menor id; como você
-- tem 1 motoboy, é ele.
-- ---------------------------------------------------------------------
-- (a) Vincula a conta de login ao motoboy:
-- update public.motoboy
--    set email   = 'entregador@teste.com',
--        auth_id = (select id from auth.users where email = 'entregador@teste.com')
--  where id = (select min(id) from public.motoboy);
--
-- (b) Cria um pedido de TESTE já "enviado" a esse motoboy:
-- insert into public.pedidos
--   (id_usuario, id_receita, endereco_entrega, opcao_pagamento, status, valor_total, observacao, id_motoboy)
-- select
--   (select min(id) from public.usuarios),
--   (select min(id) from public.receitas),
--   'Rua Santos Domingos, 219 - Centro, São Paulo - CEP 01001-000',
--   'PIX', 'Saiu para entrega', 50, 'TESTE APP ENTREGADOR',
--   (select min(id) from public.motoboy);
--
-- (c) Para conferir depois de tocar em "Concluir" no app:
-- select id, status, id_motoboy, observacao from public.pedidos order by id desc limit 5;
--
-- (d) Para APAGAR o pedido de teste quando terminar:
-- delete from public.pedidos where observacao = 'TESTE APP ENTREGADOR';
