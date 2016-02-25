package org.osgp.adapter.protocol.dlms.domain.commands;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.openmuc.jdlms.LnClientConnection;
import org.openmuc.jdlms.MethodParameter;
import org.openmuc.jdlms.MethodResultCode;
import org.openmuc.jdlms.SecurityUtils;
import org.openmuc.jdlms.SecurityUtils.KeyId;
import org.osgp.adapter.protocol.dlms.domain.entities.DlmsDevice;
import org.osgp.adapter.protocol.dlms.domain.entities.SecurityKey;
import org.osgp.adapter.protocol.dlms.domain.entities.SecurityKeyType;
import org.osgp.adapter.protocol.dlms.domain.repositories.DlmsDeviceRepository;
import org.osgp.adapter.protocol.dlms.exceptions.ProtocolAdapterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReplaceKeyCommandExecutor implements CommandExecutor<ReplaceKeyCommandExecutor.KeyWrapper, DlmsDevice> {

    static class KeyWrapper {
        private final byte[] bytes;
        private final KeyId keyId;
        private final SecurityKeyType securityKeyType;

        public KeyWrapper(final byte[] bytes, final KeyId keyId, final SecurityKeyType securityKeyType) {
            this.bytes = bytes;
            this.keyId = keyId;
            this.securityKeyType = securityKeyType;
        }

        public byte[] getBytes() {
            return this.bytes;
        }

        public KeyId getKeyId() {
            return this.keyId;
        }

        public SecurityKeyType getSecurityKeyType() {
            return this.securityKeyType;
        }
    }

    @Autowired
    private DlmsDeviceRepository dlmsDeviceRepository;

    public static KeyWrapper wrap(final byte[] bytes, final KeyId keyId, final SecurityKeyType securityKeyType) {
        return new KeyWrapper(bytes, keyId, securityKeyType);
    }

    @Override
    public DlmsDevice execute(final LnClientConnection conn, final DlmsDevice device,
            final ReplaceKeyCommandExecutor.KeyWrapper keyWrapper) throws IOException, TimeoutException,
            ProtocolAdapterException {

        // Add the new key and store in the repo
        DlmsDevice devicePostSave = this.storeNewKey(device, keyWrapper.getBytes(), keyWrapper.getSecurityKeyType());

        // Send the key to the device.
        this.sendToDevice(conn, devicePostSave, keyWrapper);

        // Update key status
        devicePostSave = this.storeNewKeyState(devicePostSave, keyWrapper.getSecurityKeyType());

        return devicePostSave;
    }

    /**
     * Send the key to the device.
     *
     * @param conn
     *            jDLMS connection.
     * @param device
     *            Device instance
     * @param keyWrapper
     *            Key data
     * @throws IOException
     * @throws ProtocolAdapterException
     */
    private void sendToDevice(final LnClientConnection conn, final DlmsDevice device,
            final ReplaceKeyCommandExecutor.KeyWrapper keyWrapper) throws IOException, ProtocolAdapterException {
        final MethodParameter methodParameterAuth = SecurityUtils.globalKeyTransfer(this.getMasterKey(device),
                keyWrapper.getBytes(), keyWrapper.getKeyId());
        final MethodResultCode methodResultCode = conn.action(methodParameterAuth).get(0).resultCode();

        if (!MethodResultCode.SUCCESS.equals(methodResultCode)) {
            throw new ProtocolAdapterException("AccessResultCode for replace keys was not SUCCESS: " + methodResultCode);
        }
    }

    /**
     * Get the valid master key from the device.
     *
     * @param device
     *            Device instance
     * @return The valid master key.
     * @throws ProtocolAdapterException
     *             when master key can not be decoded to a valid hex value.
     */
    private byte[] getMasterKey(final DlmsDevice device) throws ProtocolAdapterException {
        try {
            final SecurityKey masterKey = device.getValidSecurityKey(SecurityKeyType.E_METER_MASTER);
            return Hex.decodeHex(masterKey.getKey().toCharArray());
        } catch (final DecoderException e) {
            throw new ProtocolAdapterException("Error while decoding key hex string.", e);
        }
    }

    /**
     * Store new key
     *
     * CAUTION: only call when a successful connection with the device has been
     * made, and you are sure any existing new key data is NOT VALID.
     *
     * @param device
     *            Device
     * @param key
     *            Key data
     * @param securityKeyType
     *            Type of key
     * @return Saved device
     */
    private DlmsDevice storeNewKey(final DlmsDevice device, final byte[] key, final SecurityKeyType securityKeyType) {
        // If a new key exists, delete this key.
        final SecurityKey existingKey = device.getNewSecurityKey(securityKeyType);
        if (existingKey != null) {
            device.getSecurityKeys().remove(existingKey);
        }

        device.addSecurityKey(new SecurityKey(device, securityKeyType, Hex.encodeHexString(key), null, null));
        return this.dlmsDeviceRepository.save(device);
    }

    /**
     * Store new key state
     *
     * @param device
     *            Device
     * @param securityKeyType
     *            Type of key
     * @return Saved device
     */
    private DlmsDevice storeNewKeyState(final DlmsDevice device, final SecurityKeyType securityKeyType) {
        final Date now = new Date();
        device.getValidSecurityKey(securityKeyType).setValidTo(now);
        device.getNewSecurityKey(securityKeyType).setValidFrom(now);
        return this.dlmsDeviceRepository.save(device);
    }
}
