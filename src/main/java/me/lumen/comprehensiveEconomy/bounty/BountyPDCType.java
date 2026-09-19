package me.lumen.comprehensiveEconomy.bounty;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;

/**
 * A wrapper type for storing a bounty in PDC
 */
public class BountyPDCType implements PersistentDataType<byte[], Optional<Bounty>> {
    
    public static final BountyPDCType TYPE = new BountyPDCType();
    private BountyPDCType() {}
    @Override
    public @NotNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull Class<Optional<Bounty>> getComplexType() {
        return (Class<Optional<Bounty>>) (Class<?>) Optional.class;
    }

    @Override
    public byte @NonNull [] toPrimitive(@NonNull Optional<Bounty> complex, @NotNull PersistentDataAdapterContext context) {
        if (complex.isEmpty()) return new byte[0];
        UUID uuid = complex.get().uuid();
        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES * 2);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }

    @Override
    public @NonNull Optional<Bounty> fromPrimitive(byte @NonNull [] primitive, @NotNull PersistentDataAdapterContext context) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(primitive);
        long mostSignificant = byteBuffer.getLong();
        long leastSignificant = byteBuffer.getLong();
        UUID uuid = new UUID(mostSignificant, leastSignificant);
        
        Bounty bounty = Bounty.getBounty(uuid);
        if (bounty == null) return Optional.empty();
        return Optional.of(bounty);
    }
}
