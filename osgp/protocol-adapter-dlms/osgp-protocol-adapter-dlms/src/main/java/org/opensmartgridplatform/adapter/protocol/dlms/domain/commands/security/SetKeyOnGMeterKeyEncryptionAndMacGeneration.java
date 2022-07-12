/*
 * Copyright 2022 Alliander N.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.security;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Hex;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.mbus.IdentificationNumber;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.mbus.ManufacturerId;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.entities.DlmsDevice;
import org.opensmartgridplatform.adapter.protocol.dlms.exceptions.ProtocolAdapterException;

@Slf4j
public class SetKeyOnGMeterKeyEncryptionAndMacGeneration {

  private static final int IV_LENGTH = 12;
  private static final int MBUS_VERSION = 6;
  private static final int MEDIUM = 3; // Gas

  /**
   * Encrypts a new M-Bus User key with the M-Bus Default key for use as M-Bus Client Setup
   * transfer_key parameter.
   *
   * <p>Note that the specifics of the encryption of the M-Bus User key depend on the M-Bus version
   * the devices support. This method should be appropriate for use with DSMR 4 M-Bus devices.
   *
   * <p>The encryption is performed by applying an AES/CBC/NoPadding cipher initialized for
   * encryption with the given mbusDefaultKey and an initialization vector of 16 zero-bytes to the
   * given mbusUserKey.
   *
   * @return the properly wrapped User key for a DSMR 4 M-Bus User key change.
   */
  public byte[] encryptMbusUserKeyDsmr4(final byte[] mbusDefaultKey, final byte[] mbusUserKey)
      throws ProtocolAdapterException {

    final Key secretkeySpec = new SecretKeySpec(mbusDefaultKey, "AES");

    try {
      final Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

      @SuppressWarnings("java:S3329") // IV is always the same, this is defined in the P2 standard.
      final IvParameterSpec params = new IvParameterSpec(new byte[16]);

      cipher.init(Cipher.ENCRYPT_MODE, secretkeySpec, params);
      return cipher.doFinal(mbusUserKey);

    } catch (final NoSuchAlgorithmException
        | NoSuchPaddingException
        | InvalidKeyException
        | InvalidAlgorithmParameterException
        | IllegalBlockSizeException
        | BadPaddingException e) {
      final String message = "Error encrypting M-Bus User key with M-Bus Default key for transfer.";
      log.error(message, e);
      throw new ProtocolAdapterException(message);
    }
  }

  /**
   * Encrypts a new M-Bus User key with the M-Bus Default key for use as M-Bus Client Setup
   * transfer_key parameter and adds a GCM Authentication tag
   *
   * <p>Note that the specifics of the encryption of the M-Bus User key depend on the M-Bus version
   * the devices support. This method should be appropriate for use with SMR 5 M-Bus devices.
   *
   * <p>The encryption is performed by applying an AES/GCM/NoPadding cipher initialized for
   * encryption with the given mbusDefaultKey and an initialization vector containing a key change
   * counter. If the kcc is null, then the number of seconds since january 2000 is used.
   *
   * @return the properly wrapped User key for a DSMR 4 M-Bus User key change.
   */
  public byte[] encryptAndAddGcmAuthenticationTagSmr5(
      final DlmsDevice device,
      final int keyId,
      final int keySize,
      final Integer kcc,
      final byte[] masterKey,
      final byte[] newKey)
      throws ProtocolAdapterException {

    final byte[] iv = this.createIV(device, kcc, MBUS_VERSION, MEDIUM);

    log.debug("Calculated IV: {}", Hex.toHexString(iv));

    final byte[] keyData = new byte[newKey.length + 2];
    keyData[0] = (byte) keyId;
    keyData[1] = (byte) keySize;
    System.arraycopy(newKey, 0, keyData, 2, newKey.length);

    log.debug("Key data: {}", Hex.toHexString(keyData));

    final byte[] encryptedKeyData =
        this.encryptAndAddGcmAuthenticationTagWithIv(masterKey, keyData, iv);

    log.debug("Encrypted key data: {}", Hex.toHexString(encryptedKeyData));

    return encryptedKeyData;
  }

  protected byte[] createIV(
      final DlmsDevice device, final Integer kcc, final int mBusVersion, final int medium)
      throws ProtocolAdapterException {
    return ByteBuffer.allocate(IV_LENGTH)
        .put(Arrays.reverse(this.getMbusIdentificationNnumber(device)))
        .put(this.getMbusManufacturerId(device))
        .put((byte) mBusVersion)
        .put((byte) medium)
        .put(this.getKCC(kcc))
        .array();
  }

  private byte[] getKCC(final Integer kcc) throws ProtocolAdapterException {
    if (kcc != null) {
      final byte[] byteArrayWithKcc = BigInteger.valueOf(kcc).toByteArray();
      return this.addPadding(byteArrayWithKcc, 4);
    } else {
      // If kcc is null, then use number of seconds since 2000
      final LocalDateTime january2000 = LocalDateTime.of(2000, 1, 1, 0, 0);
      final long numberOfSeconds = ChronoUnit.SECONDS.between(january2000, LocalDateTime.now());
      return BigInteger.valueOf(numberOfSeconds).toByteArray();
    }
  }

  private byte[] getMbusManufacturerId(final DlmsDevice device) {
    final String mbusManufacturerIdentification = device.getMbusManufacturerIdentification();
    final ManufacturerId manufacturerId =
        ManufacturerId.fromIdentification(mbusManufacturerIdentification);
    return BigInteger.valueOf(manufacturerId.getId()).toByteArray();
  }

  private byte[] getMbusIdentificationNnumber(final DlmsDevice device) {
    final String mbusIdentificationNumber = device.getMbusIdentificationNumber();
    final IdentificationNumber identificationNumber =
        IdentificationNumber.fromTextualRepresentation(mbusIdentificationNumber);
    return BigInteger.valueOf(
            identificationNumber.getIdentificationNumberInBcdRepresentationAsLong())
        .toByteArray();
  }

  private byte[] addPadding(final byte[] input, final int size) throws ProtocolAdapterException {
    if (input.length > size) {
      throw new ProtocolAdapterException(
          String.format(
              "Input for padding should not be larger (%d) than size (%d)", input.length, size));
    }

    final byte[] output = new byte[size];
    System.arraycopy(input, 0, output, size - input.length, input.length);

    return output;
  }

  private byte[] encryptAndAddGcmAuthenticationTagWithIv(
      final byte[] mbusDefaultKey, final byte[] mbusUserKey, final byte[] iv)
      throws ProtocolAdapterException {
    Security.addProvider(new BouncyCastleProvider());

    final Key secretkeySpec = new SecretKeySpec(mbusDefaultKey, "AES");

    try {
      final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
      final GCMParameterSpec parameterSpec =
          new GCMParameterSpec(64, iv); // 128 bit auth tag length
      cipher.init(Cipher.ENCRYPT_MODE, secretkeySpec, parameterSpec);
      return cipher.doFinal(mbusUserKey);

    } catch (final NoSuchAlgorithmException
        | NoSuchPaddingException
        | InvalidKeyException
        | InvalidAlgorithmParameterException
        | IllegalBlockSizeException
        | BadPaddingException
        | NoSuchProviderException e) {
      final String message = "Error encrypting M-Bus User key with M-Bus Default key for transfer.";
      log.error(message, e);
      throw new ProtocolAdapterException(message);
    }
  }
}
