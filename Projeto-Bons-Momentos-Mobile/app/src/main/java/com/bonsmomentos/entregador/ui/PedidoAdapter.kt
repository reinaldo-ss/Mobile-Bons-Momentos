package com.bonsmomentos.entregador.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import android.transition.TransitionManager
import com.bonsmomentos.entregador.R
import com.bonsmomentos.entregador.data.local.PedidoEntity
import com.bonsmomentos.entregador.databinding.ItemPedidoBinding

/** Lista de pedidos em formato "acordeão": toca no topo para abrir/fechar os detalhes. */
class PedidoAdapter(
    private val aoConcluir: (PedidoEntity) -> Unit
) : ListAdapter<PedidoEntity, PedidoAdapter.VH>(Diff) {

    /** Guarda quais pedidos estão abertos (sobrevive à reciclagem das linhas). */
    private val abertos = mutableSetOf<Long>()

    inner class VH(val b: ItemPedidoBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemPedidoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = getItem(position)
        val b = holder.b
        val ctx = b.root.context

        b.tvTitulo.text = "Pedido - " + p.id.toString().padStart(2, '0')
        b.tvProduto.text = linha(ctx, "Produto:", p.produto)
        b.tvPagamento.text = linha(ctx, "Pagamento:", p.pagamento)
        b.tvCep.text = linha(ctx, "CEP:", p.cep)
        b.tvEndereco.text = linha(ctx, "Endereço:", p.endereco)

        aplicarEstado(b, p.id in abertos)

        b.llTopo.setOnClickListener {
            val abrir = p.id !in abertos
            if (abrir) abertos.add(p.id) else abertos.remove(p.id)
            TransitionManager.beginDelayedTransition(b.root as ViewGroup)
            aplicarEstado(b, abrir)
        }
        b.btnConcluir.setOnClickListener { aoConcluir(p) }
    }

    private fun aplicarEstado(b: ItemPedidoBinding, aberto: Boolean) {
        b.llDetalhes.visibility = if (aberto) View.VISIBLE else View.GONE
        b.ivSeta.rotation = if (aberto) 180f else 0f
    }

    /** "Rótulo:" em negrito marrom + valor em cinza (igual ao front). */
    private fun linha(ctx: Context, rotulo: String, valor: String): CharSequence =
        buildSpannedString {
            color(ContextCompat.getColor(ctx, R.color.bm_marrom_escuro)) {
                bold { append(rotulo) }
            }
            append(" ")
            append(valor.ifBlank { "—" })
        }

    private object Diff : DiffUtil.ItemCallback<PedidoEntity>() {
        override fun areItemsTheSame(a: PedidoEntity, b: PedidoEntity) = a.id == b.id
        override fun areContentsTheSame(a: PedidoEntity, b: PedidoEntity) = a == b
    }
}
