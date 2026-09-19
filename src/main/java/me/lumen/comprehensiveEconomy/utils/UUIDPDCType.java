package me.lumen.comprehensiveEconomy.utils;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * A type for a uuid in pdc
 * Stolen from paper documentation for {@link PersistentDataType}, but I ended up needing to use it
 */
public class UUIDPDCType implements PersistentDataType<byte[], UUID> {
    private UUIDPDCType() {}
    public static final UUIDPDCType TYPE = new UUIDPDCType();

    @Override
    public @NonNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public @NonNull Class<UUID> getComplexType() {
        return UUID.class;
    }

    @Override
    public byte @NonNull [] toPrimitive(@NonNull UUID complex, @NonNull PersistentDataAdapterContext context) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(complex.getMostSignificantBits());
        bb.putLong(complex.getLeastSignificantBits());
        return bb.array();
    }

    @Override
    public @NonNull UUID fromPrimitive(byte @NonNull [] primitive, @NonNull PersistentDataAdapterContext context) {
        ByteBuffer bb = ByteBuffer.wrap(primitive);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }
}
