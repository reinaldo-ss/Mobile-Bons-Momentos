package com.bonsmomentos.entregador.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PedidoDao_Impl implements PedidoDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PedidoEntity> __insertionAdapterOfPedidoEntity;

  private final SharedSQLiteStatement __preparedStmtOfAtualizarStatusLocal;

  private final SharedSQLiteStatement __preparedStmtOfMarcarSincronizada;

  private final SharedSQLiteStatement __preparedStmtOfLimparTudo;

  public PedidoDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPedidoEntity = new EntityInsertionAdapter<PedidoEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `pedidos` (`id`,`produto`,`pagamento`,`cep`,`endereco`,`status`,`syncPendente`,`atualizadoEm`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PedidoEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getProduto());
        statement.bindString(3, entity.getPagamento());
        statement.bindString(4, entity.getCep());
        statement.bindString(5, entity.getEndereco());
        statement.bindString(6, entity.getStatus());
        final int _tmp = entity.getSyncPendente() ? 1 : 0;
        statement.bindLong(7, _tmp);
        statement.bindLong(8, entity.getAtualizadoEm());
      }
    };
    this.__preparedStmtOfAtualizarStatusLocal = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE pedidos SET status = ?, syncPendente = 1, atualizadoEm = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarcarSincronizada = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE pedidos SET syncPendente = 0 WHERE id = ? AND atualizadoEm = ?";
        return _query;
      }
    };
    this.__preparedStmtOfLimparTudo = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM pedidos";
        return _query;
      }
    };
  }

  @Override
  public Object upsertAll(final List<PedidoEntity> lista,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPedidoEntity.insert(lista);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object atualizarStatusLocal(final long id, final String status, final long ts,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfAtualizarStatusLocal.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, status);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, ts);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfAtualizarStatusLocal.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object marcarSincronizada(final long id, final long ts,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarcarSincronizada.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, ts);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarcarSincronizada.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object limparTudo(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfLimparTudo.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfLimparTudo.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<PedidoEntity>> observarDisponiveis() {
    final String _sql = "SELECT * FROM pedidos WHERE status != 'ENTREGUE' ORDER BY id";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"pedidos"}, new Callable<List<PedidoEntity>>() {
      @Override
      @NonNull
      public List<PedidoEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfProduto = CursorUtil.getColumnIndexOrThrow(_cursor, "produto");
          final int _cursorIndexOfPagamento = CursorUtil.getColumnIndexOrThrow(_cursor, "pagamento");
          final int _cursorIndexOfCep = CursorUtil.getColumnIndexOrThrow(_cursor, "cep");
          final int _cursorIndexOfEndereco = CursorUtil.getColumnIndexOrThrow(_cursor, "endereco");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfSyncPendente = CursorUtil.getColumnIndexOrThrow(_cursor, "syncPendente");
          final int _cursorIndexOfAtualizadoEm = CursorUtil.getColumnIndexOrThrow(_cursor, "atualizadoEm");
          final List<PedidoEntity> _result = new ArrayList<PedidoEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PedidoEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpProduto;
            _tmpProduto = _cursor.getString(_cursorIndexOfProduto);
            final String _tmpPagamento;
            _tmpPagamento = _cursor.getString(_cursorIndexOfPagamento);
            final String _tmpCep;
            _tmpCep = _cursor.getString(_cursorIndexOfCep);
            final String _tmpEndereco;
            _tmpEndereco = _cursor.getString(_cursorIndexOfEndereco);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpSyncPendente;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSyncPendente);
            _tmpSyncPendente = _tmp != 0;
            final long _tmpAtualizadoEm;
            _tmpAtualizadoEm = _cursor.getLong(_cursorIndexOfAtualizadoEm);
            _item = new PedidoEntity(_tmpId,_tmpProduto,_tmpPagamento,_tmpCep,_tmpEndereco,_tmpStatus,_tmpSyncPendente,_tmpAtualizadoEm);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> observarQtdPendentes() {
    final String _sql = "SELECT COUNT(*) FROM pedidos WHERE syncPendente = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"pedidos"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object contar(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM pedidos";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object pendentes(final Continuation<? super List<PedidoEntity>> $completion) {
    final String _sql = "SELECT * FROM pedidos WHERE syncPendente = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PedidoEntity>>() {
      @Override
      @NonNull
      public List<PedidoEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfProduto = CursorUtil.getColumnIndexOrThrow(_cursor, "produto");
          final int _cursorIndexOfPagamento = CursorUtil.getColumnIndexOrThrow(_cursor, "pagamento");
          final int _cursorIndexOfCep = CursorUtil.getColumnIndexOrThrow(_cursor, "cep");
          final int _cursorIndexOfEndereco = CursorUtil.getColumnIndexOrThrow(_cursor, "endereco");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfSyncPendente = CursorUtil.getColumnIndexOrThrow(_cursor, "syncPendente");
          final int _cursorIndexOfAtualizadoEm = CursorUtil.getColumnIndexOrThrow(_cursor, "atualizadoEm");
          final List<PedidoEntity> _result = new ArrayList<PedidoEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PedidoEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpProduto;
            _tmpProduto = _cursor.getString(_cursorIndexOfProduto);
            final String _tmpPagamento;
            _tmpPagamento = _cursor.getString(_cursorIndexOfPagamento);
            final String _tmpCep;
            _tmpCep = _cursor.getString(_cursorIndexOfCep);
            final String _tmpEndereco;
            _tmpEndereco = _cursor.getString(_cursorIndexOfEndereco);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpSyncPendente;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSyncPendente);
            _tmpSyncPendente = _tmp != 0;
            final long _tmpAtualizadoEm;
            _tmpAtualizadoEm = _cursor.getLong(_cursorIndexOfAtualizadoEm);
            _item = new PedidoEntity(_tmpId,_tmpProduto,_tmpPagamento,_tmpCep,_tmpEndereco,_tmpStatus,_tmpSyncPendente,_tmpAtualizadoEm);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object removerAusentes(final List<Long> idsRemotos,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM pedidos WHERE syncPendente = 0 AND id NOT IN (");
        final int _inputSize = idsRemotos.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (long _item : idsRemotos) {
          _stmt.bindLong(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
