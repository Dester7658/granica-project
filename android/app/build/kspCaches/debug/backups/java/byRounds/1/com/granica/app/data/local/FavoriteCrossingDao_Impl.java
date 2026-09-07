package com.granica.app.data.local;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Boolean;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
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
public final class FavoriteCrossingDao_Impl implements FavoriteCrossingDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<FavoriteCrossingEntity> __insertionAdapterOfFavoriteCrossingEntity;

  private final EntityDeletionOrUpdateAdapter<FavoriteCrossingEntity> __deletionAdapterOfFavoriteCrossingEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public FavoriteCrossingDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFavoriteCrossingEntity = new EntityInsertionAdapter<FavoriteCrossingEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `favorite_crossings` (`crossingId`,`name`,`countryCode`,`addedAtMillis`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FavoriteCrossingEntity entity) {
        statement.bindString(1, entity.getCrossingId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getCountryCode());
        statement.bindLong(4, entity.getAddedAtMillis());
      }
    };
    this.__deletionAdapterOfFavoriteCrossingEntity = new EntityDeletionOrUpdateAdapter<FavoriteCrossingEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `favorite_crossings` WHERE `crossingId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FavoriteCrossingEntity entity) {
        statement.bindString(1, entity.getCrossingId());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM favorite_crossings WHERE crossingId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final FavoriteCrossingEntity favorite,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFavoriteCrossingEntity.insert(favorite);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final FavoriteCrossingEntity favorite,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfFavoriteCrossingEntity.handle(favorite);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final String crossingId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, crossingId);
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
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<FavoriteCrossingEntity>> observeAll() {
    final String _sql = "SELECT * FROM favorite_crossings ORDER BY addedAtMillis DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"favorite_crossings"}, new Callable<List<FavoriteCrossingEntity>>() {
      @Override
      @NonNull
      public List<FavoriteCrossingEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCrossingId = CursorUtil.getColumnIndexOrThrow(_cursor, "crossingId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCountryCode = CursorUtil.getColumnIndexOrThrow(_cursor, "countryCode");
          final int _cursorIndexOfAddedAtMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAtMillis");
          final List<FavoriteCrossingEntity> _result = new ArrayList<FavoriteCrossingEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FavoriteCrossingEntity _item;
            final String _tmpCrossingId;
            _tmpCrossingId = _cursor.getString(_cursorIndexOfCrossingId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpCountryCode;
            _tmpCountryCode = _cursor.getString(_cursorIndexOfCountryCode);
            final long _tmpAddedAtMillis;
            _tmpAddedAtMillis = _cursor.getLong(_cursorIndexOfAddedAtMillis);
            _item = new FavoriteCrossingEntity(_tmpCrossingId,_tmpName,_tmpCountryCode,_tmpAddedAtMillis);
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
  public Flow<Boolean> observeIsFavorite(final String crossingId) {
    final String _sql = "SELECT EXISTS(SELECT 1 FROM favorite_crossings WHERE crossingId = ?)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, crossingId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"favorite_crossings"}, new Callable<Boolean>() {
      @Override
      @NonNull
      public Boolean call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Boolean _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp != 0;
          } else {
            _result = false;
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
