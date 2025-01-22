// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.protocol.dlms.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensmartgridplatform.adapter.protocol.dlms.application.wsclient.SecretManagementClient;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.entities.SecurityKeyType;
import org.opensmartgridplatform.adapter.protocol.dlms.exceptions.StoreNewKeyException;
import org.opensmartgridplatform.shared.infra.jms.MessageMetadata;
import org.opensmartgridplatform.shared.security.RsaEncrypter;
import org.opensmartgridplatform.ws.schema.core.secret.management.ActivateSecretsRequest;
import org.opensmartgridplatform.ws.schema.core.secret.management.GenerateAndStoreSecretsResponse;
import org.opensmartgridplatform.ws.schema.core.secret.management.GetSecretsRequest;
import org.opensmartgridplatform.ws.schema.core.secret.management.GetSecretsResponse;
import org.opensmartgridplatform.ws.schema.core.secret.management.HasNewSecretRequest;
import org.opensmartgridplatform.ws.schema.core.secret.management.HasNewSecretResponse;
import org.opensmartgridplatform.ws.schema.core.secret.management.OsgpResultType;
import org.opensmartgridplatform.ws.schema.core.secret.management.SecretType;
import org.opensmartgridplatform.ws.schema.core.secret.management.StoreSecretsRequest;
import org.opensmartgridplatform.ws.schema.core.secret.management.StoreSecretsResponse;
import org.opensmartgridplatform.ws.schema.core.secret.management.TypedSecret;
import org.opensmartgridplatform.ws.schema.core.secret.management.TypedSecrets;

@ExtendWith(MockitoExtension.class)
class SecretManagementServiceTest {
  private static final String DEVICE_IDENTIFICATION = "E000123456789";
  private static final SecurityKeyType KEY_TYPE = SecurityKeyType.E_METER_ENCRYPTION;
  private static final byte[] UNENCRYPTED_SECRET = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
  private static final byte[] SOAP_SECRET = {15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
  private static final String HEX_SOAP_SECRET = Hex.encodeHexString(SOAP_SECRET);
  private static final TypedSecret TYPED_SECRET = new TypedSecret();

  private static MessageMetadata messageMetadata;

  @Mock SecretManagementClient secretManagementClient;

  @Mock RsaEncrypter encrypterForSecretManagement;

  @Mock RsaEncrypter decrypterForProtocolAdapterDlms;

  private SecretManagementService secretManagementTestService;

  @BeforeEach
  public void init() {
    this.secretManagementTestService =
        new SecretManagementService(
            this.encrypterForSecretManagement,
            this.decrypterForProtocolAdapterDlms,
            this.secretManagementClient);
    TYPED_SECRET.setType(KEY_TYPE.toSecretType());
    TYPED_SECRET.setSecret(HEX_SOAP_SECRET);
    messageMetadata = MessageMetadata.newBuilder().withCorrelationUid("123456").build();
  }

  @Test
  void testGetKeys() {
    final List<SecurityKeyType> keyTypes = List.of(KEY_TYPE);
    final GetSecretsResponse response = new GetSecretsResponse();
    response.setResult(OsgpResultType.OK);
    response.setTypedSecrets(new TypedSecrets());
    response.getTypedSecrets().getTypedSecret().add(TYPED_SECRET);
    when(this.secretManagementClient.getSecretsRequest(
            same(messageMetadata), any(GetSecretsRequest.class)))
        .thenReturn(response);
    when(this.decrypterForProtocolAdapterDlms.decrypt(SOAP_SECRET)).thenReturn(UNENCRYPTED_SECRET);

    final Map<SecurityKeyType, byte[]> result =
        this.secretManagementTestService.getKeys(messageMetadata, DEVICE_IDENTIFICATION, keyTypes);

    assertThat(result).isNotNull().hasSize(1);
    assertThat(result.keySet().iterator().next()).isEqualTo(KEY_TYPE);
    assertThat(result.values().iterator().next()).isEqualTo(UNENCRYPTED_SECRET);
  }

  @Test
  void testStoreNewKeys() {
    final Map<SecurityKeyType, byte[]> keys = new EnumMap<>(SecurityKeyType.class);
    keys.put(KEY_TYPE, UNENCRYPTED_SECRET);
    final StoreSecretsResponse response = new StoreSecretsResponse();
    response.setResult(OsgpResultType.OK);
    when(this.encrypterForSecretManagement.encrypt(UNENCRYPTED_SECRET)).thenReturn(SOAP_SECRET);
    when(this.secretManagementClient.storeSecretsRequest(same(messageMetadata), any()))
        .thenReturn(response);

    this.secretManagementTestService.storeNewKeys(messageMetadata, DEVICE_IDENTIFICATION, keys);

    verify(this.secretManagementClient, times(1))
        .storeSecretsRequest(same(messageMetadata), any(StoreSecretsRequest.class));
  }

  @Test
  void testStoreNewKeysThrowsException() {
    final Map<SecurityKeyType, byte[]> keys = new EnumMap<>(SecurityKeyType.class);
    keys.put(KEY_TYPE, UNENCRYPTED_SECRET);

    when(this.encrypterForSecretManagement.encrypt(UNENCRYPTED_SECRET)).thenReturn(SOAP_SECRET);
    when(this.secretManagementClient.storeSecretsRequest(same(messageMetadata), any()))
        .thenThrow(new RuntimeException("Simulated exception"));

    assertThatThrownBy(
            () -> {
              this.secretManagementTestService.storeNewKeys(
                  messageMetadata, DEVICE_IDENTIFICATION, keys);
            })
        .isInstanceOf(StoreNewKeyException.class);
  }

  @Test
  void testActivateKeys() {
    final List<SecurityKeyType> keyTypes = List.of(KEY_TYPE);
    final ArgumentCaptor<ActivateSecretsRequest> activateSecretsCaptor =
        ArgumentCaptor.forClass(ActivateSecretsRequest.class);
    // EXECUTE
    this.secretManagementTestService.activateNewKeys(
        messageMetadata, DEVICE_IDENTIFICATION, keyTypes);
    // ASSERT
    verify(this.secretManagementClient)
        .activateSecretsRequest(same(messageMetadata), activateSecretsCaptor.capture());
    final ActivateSecretsRequest capturedArgument = activateSecretsCaptor.getValue();
    assertThat(capturedArgument.getDeviceId()).isEqualTo(DEVICE_IDENTIFICATION);
    assertThat(capturedArgument.getSecretTypes().getSecretType().get(0))
        .isEqualTo(KEY_TYPE.toSecretType());
  }

  @Test
  void testGenerateAndStoreKeys() {
    final List<SecurityKeyType> keyTypes = List.of(KEY_TYPE);
    final GenerateAndStoreSecretsResponse response = new GenerateAndStoreSecretsResponse();
    response.setResult(OsgpResultType.OK);
    response.setTypedSecrets(new TypedSecrets());
    response.getTypedSecrets().getTypedSecret().add(TYPED_SECRET);
    when(this.secretManagementClient.generateAndStoreSecrets(same(messageMetadata), any()))
        .thenReturn(response);
    when(this.decrypterForProtocolAdapterDlms.decrypt(SOAP_SECRET)).thenReturn(UNENCRYPTED_SECRET);

    final Map<SecurityKeyType, byte[]> keys =
        this.secretManagementTestService.generate128BitsKeysAndStoreAsNewKeys(
            messageMetadata, DEVICE_IDENTIFICATION, keyTypes);

    assertThat(keys).containsEntry(KEY_TYPE, UNENCRYPTED_SECRET);
  }

  @Test
  void testHasNewSecretAuthenticationKey() {
    final HasNewSecretResponse responseTrue = new HasNewSecretResponse();
    responseTrue.setHasNewSecret(true);
    final HasNewSecretResponse responseFalse = new HasNewSecretResponse();
    responseFalse.setHasNewSecret(false);

    when(this.secretManagementClient.hasNewSecretRequest(same(messageMetadata), any()))
        .thenAnswer(
            invocationOnMock -> {
              if (((HasNewSecretRequest) invocationOnMock.getArgument(1))
                  .getSecretType()
                  .equals(SecretType.E_METER_AUTHENTICATION_KEY)) {
                return responseTrue;
              } else {
                return responseFalse;
              }
            });

    final boolean result =
        this.secretManagementTestService.hasNewSecret(messageMetadata, DEVICE_IDENTIFICATION);

    assertThat(result).isTrue();
  }

  @Test
  void testHasNewSecretEncryptionKey() {
    final HasNewSecretResponse responseTrue = new HasNewSecretResponse();
    responseTrue.setHasNewSecret(true);
    final HasNewSecretResponse responseFalse = new HasNewSecretResponse();
    responseFalse.setHasNewSecret(false);

    when(this.secretManagementClient.hasNewSecretRequest(same(messageMetadata), any()))
        .thenAnswer(
            invocationOnMock -> {
              if (((HasNewSecretRequest) invocationOnMock.getArgument(1))
                  .getSecretType()
                  .equals(SecretType.E_METER_ENCRYPTION_KEY_UNICAST)) {
                return responseTrue;
              } else {
                return responseFalse;
              }
            });

    final boolean result =
        this.secretManagementTestService.hasNewSecret(messageMetadata, DEVICE_IDENTIFICATION);

    assertThat(result).isTrue();
  }
}
