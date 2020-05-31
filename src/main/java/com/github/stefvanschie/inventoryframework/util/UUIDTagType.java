package com.github.stefvanschie.inventoryframework.util;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * A {@link PersistentDataType} implementation that adds support for {@link UUID}s.
 *
 * @since 0.6.0
 */
public final class UUIDTagType implements PersistentDataType<byte[], UUID> {
	
	/**
	 * The one and only instance of this class.
	 * Since this class stores no state information (apart from this field),
	 * the usage of a single instance is safe even across multiple threads.
	 */
	public static final UUIDTagType INSTANCE = new UUIDTagType();
	
	/**
	 * A private constructor so that only a single instance of this class can exist.
	 */
	private UUIDTagType() {}
	
	@NotNull
	@Override
	public Class<byte[]> getPrimitiveType() {
		return byte[].class;
	}
	
	@NotNull
	@Override
	public Class<UUID> getComplexType() {
		return UUID.class;
	}
	
	@NotNull
	@Override
	public byte[] toPrimitive(@NotNull UUID complex, @NotNull PersistentDataAdapterContext context) {
		ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
		buffer.putLong(complex.getMostSignificantBits());
		buffer.putLong(complex.getLeastSignificantBits());
		return buffer.array();
	}
	
	@NotNull
	@Override
	public UUID fromPrimitive(@NotNull byte[] primitive, @NotNull PersistentDataAdapterContext context) {
		ByteBuffer buffer = ByteBuffer.wrap(primitive);
		long most = buffer.getLong();
		long least = buffer.getLong();
		return new UUID(most, least);
	}
}
