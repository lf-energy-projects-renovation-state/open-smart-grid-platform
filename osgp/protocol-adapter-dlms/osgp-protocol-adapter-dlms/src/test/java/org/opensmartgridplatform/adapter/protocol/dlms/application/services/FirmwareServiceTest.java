// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.protocol.dlms.application.services;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.firmware.GetFirmwareVersionsCommandExecutor;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.firmware.UpdateFirmwareCommandExecutor;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.entities.DlmsDevice;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.factories.DlmsConnectionManager;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.repositories.FirmwareFileCachingRepository;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.repositories.FirmwareImageIdentifierCachingRepository;
import org.opensmartgridplatform.adapter.protocol.dlms.exceptions.ProtocolAdapterException;
import org.opensmartgridplatform.dto.valueobjects.HashTypeDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.UpdateFirmwareRequestDataDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.UpdateFirmwareRequestDto;
import org.opensmartgridplatform.shared.exceptionhandling.OsgpException;
import org.opensmartgridplatform.shared.infra.jms.MessageMetadata;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class FirmwareServiceTest {

  @Mock private FirmwareFileCachingRepository firmwareFileCachingRepository;

  @Mock private FirmwareImageIdentifierCachingRepository firmwareImageIdentifierCachingRepository;

  @Mock private GetFirmwareVersionsCommandExecutor getFirmwareVersionsCommandExecutor;

  @Mock private UpdateFirmwareCommandExecutor updateFirmwareCommandExecutor;

  @Mock private DlmsConnectionManager dlmsConnectionManagerMock;

  @Mock private DlmsDevice dlmsDeviceMock;

  @Mock private FirmwareFileStoreService firmwareFileStoreService;

  @InjectMocks private FirmwareService firmwareService;

  private static MessageMetadata messageMetadata;
  private static final String FIRMWARE_IDENTIFICATION = "firmware-file-1";
  private static final String IMAGE_IDENTIFIER = "496d6167654964656e746966696572";
  private static final String DEVICE_IDENTIFICATION = "device-1";
  private static UpdateFirmwareRequestDto updateFirmwareRequestDto;
  private static final String VALID_MD5_FIRMWARE_DIGEST = "48b5773e8b37a602d38a521e98cfc6a1";

  @BeforeAll
  public static void init() {
    messageMetadata = MessageMetadata.newBuilder().withCorrelationUid("123456").build();
    updateFirmwareRequestDto =
        createUpdateFirmwareRequestDto(HashTypeDto.MD5, VALID_MD5_FIRMWARE_DIGEST);
  }

  private static UpdateFirmwareRequestDto createUpdateFirmwareRequestDto(
      final HashTypeDto hashTypeDto, final String firmwareDigest) {
    return new UpdateFirmwareRequestDto(
        DEVICE_IDENTIFICATION,
        new UpdateFirmwareRequestDataDto(FIRMWARE_IDENTIFICATION, hashTypeDto, firmwareDigest));
  }

  @Test
  void getFirmwareVersionsShouldCallExecutor() throws ProtocolAdapterException {
    this.firmwareService.getFirmwareVersions(
        this.dlmsConnectionManagerMock, this.dlmsDeviceMock, messageMetadata);

    verify(this.getFirmwareVersionsCommandExecutor, times(1))
        .execute(this.dlmsConnectionManagerMock, this.dlmsDeviceMock, null, messageMetadata);
  }

  @Test
  void updateFirmwareWhenAllInCache() throws OsgpException {
    final byte[] firmwareFile = FIRMWARE_IDENTIFICATION.getBytes();
    final byte[] firmwareImageIdentifier = Hex.decode(IMAGE_IDENTIFIER);

    when(this.firmwareFileCachingRepository.isAvailable(FIRMWARE_IDENTIFICATION)).thenReturn(true);
    when(this.firmwareFileCachingRepository.retrieve(FIRMWARE_IDENTIFICATION))
        .thenReturn(firmwareFile);
    when(this.firmwareImageIdentifierCachingRepository.isAvailable(FIRMWARE_IDENTIFICATION))
        .thenReturn(true);
    when(this.firmwareImageIdentifierCachingRepository.retrieve(FIRMWARE_IDENTIFICATION))
        .thenReturn(firmwareImageIdentifier);

    this.firmwareService.updateFirmware(
        this.dlmsConnectionManagerMock,
        this.dlmsDeviceMock,
        updateFirmwareRequestDto,
        messageMetadata);

    verifyNoInteractions(this.firmwareFileStoreService);
    verify(this.firmwareFileCachingRepository, never()).store(anyString(), any(byte[].class));
    verify(this.firmwareImageIdentifierCachingRepository, never())
        .store(anyString(), any(byte[].class));
    verify(this.updateFirmwareCommandExecutor, times(1))
        .execute(
            this.dlmsConnectionManagerMock,
            this.dlmsDeviceMock,
            updateFirmwareRequestDto,
            messageMetadata);
  }

  @Test
  void updateFirmwareWhenFirmwareFileNotInCache() throws OsgpException {
    final byte[] firmwareFile = FIRMWARE_IDENTIFICATION.getBytes();
    final byte[] firmwareImageIdentifier = Hex.decode(IMAGE_IDENTIFIER);

    when(this.firmwareFileCachingRepository.isAvailable(FIRMWARE_IDENTIFICATION)).thenReturn(false);
    when(this.firmwareFileStoreService.readFirmwareFile(FIRMWARE_IDENTIFICATION))
        .thenReturn(firmwareFile);
    when(this.firmwareFileCachingRepository.retrieve(FIRMWARE_IDENTIFICATION))
        .thenReturn(firmwareFile);
    when(this.firmwareImageIdentifierCachingRepository.isAvailable(FIRMWARE_IDENTIFICATION))
        .thenReturn(true);
    when(this.firmwareImageIdentifierCachingRepository.retrieve(FIRMWARE_IDENTIFICATION))
        .thenReturn(firmwareImageIdentifier);

    this.firmwareService.updateFirmware(
        this.dlmsConnectionManagerMock,
        this.dlmsDeviceMock,
        updateFirmwareRequestDto,
        messageMetadata);

    verify(this.firmwareFileStoreService).readFirmwareFile(FIRMWARE_IDENTIFICATION);
    verify(this.firmwareFileCachingRepository).store(FIRMWARE_IDENTIFICATION, firmwareFile);
    verify(this.firmwareImageIdentifierCachingRepository, never())
        .store(anyString(), any(byte[].class));
    verify(this.updateFirmwareCommandExecutor, times(1))
        .execute(
            this.dlmsConnectionManagerMock,
            this.dlmsDeviceMock,
            updateFirmwareRequestDto,
            messageMetadata);
  }

  @Test
  void updateFirmwareWhenImageIdentifierNotInCache() throws OsgpException {
    final byte[] firmwareFile = FIRMWARE_IDENTIFICATION.getBytes();
    final byte[] firmwareImageIdentifier = Hex.decode(IMAGE_IDENTIFIER);

    when(this.firmwareFileCachingRepository.isAvailable(FIRMWARE_IDENTIFICATION)).thenReturn(true);
    when(this.firmwareFileCachingRepository.retrieve(FIRMWARE_IDENTIFICATION))
        .thenReturn(firmwareFile);

    when(this.firmwareImageIdentifierCachingRepository.isAvailable(FIRMWARE_IDENTIFICATION))
        .thenReturn(false);
    when(this.firmwareFileStoreService.readImageIdentifier(FIRMWARE_IDENTIFICATION))
        .thenReturn(firmwareImageIdentifier);
    when(this.firmwareImageIdentifierCachingRepository.retrieve(FIRMWARE_IDENTIFICATION))
        .thenReturn(firmwareImageIdentifier);

    this.firmwareService.updateFirmware(
        this.dlmsConnectionManagerMock,
        this.dlmsDeviceMock,
        updateFirmwareRequestDto,
        messageMetadata);

    verify(this.firmwareFileStoreService).readImageIdentifier(FIRMWARE_IDENTIFICATION);
    verify(this.firmwareImageIdentifierCachingRepository)
        .store(FIRMWARE_IDENTIFICATION, firmwareImageIdentifier);
    verify(this.firmwareFileStoreService, never()).readFirmwareFile(FIRMWARE_IDENTIFICATION);
    verify(this.firmwareFileCachingRepository, never()).store(anyString(), any(byte[].class));
    verify(this.updateFirmwareCommandExecutor, times(1))
        .execute(
            this.dlmsConnectionManagerMock,
            this.dlmsDeviceMock,
            updateFirmwareRequestDto,
            messageMetadata);
  }

  @Test
  void updateFirmwareWhenFirmwareFileNotInCacheAndNotValid() throws OsgpException {

    final byte[] firmwareFile = FIRMWARE_IDENTIFICATION.getBytes();
    final HashTypeDto sha256 = HashTypeDto.SHA256;

    when(this.firmwareFileCachingRepository.isAvailable(FIRMWARE_IDENTIFICATION)).thenReturn(false);
    when(this.firmwareFileStoreService.readFirmwareFile(FIRMWARE_IDENTIFICATION))
        .thenReturn(firmwareFile);

    updateFirmwareRequestDto = createUpdateFirmwareRequestDto(sha256, VALID_MD5_FIRMWARE_DIGEST);
    assertThatExceptionOfType(ProtocolAdapterException.class)
        .isThrownBy(
            () -> {
              this.firmwareService.updateFirmware(
                  this.dlmsConnectionManagerMock,
                  this.dlmsDeviceMock,
                  updateFirmwareRequestDto,
                  messageMetadata);
            })
        .withMessageContainingAll(FIRMWARE_IDENTIFICATION, sha256.getAlgorithmName());

    verify(this.firmwareFileStoreService).readFirmwareFile(FIRMWARE_IDENTIFICATION);
    verify(this.firmwareFileCachingRepository, never())
        .store(FIRMWARE_IDENTIFICATION, firmwareFile);
    verify(this.firmwareFileCachingRepository, never()).retrieve(FIRMWARE_IDENTIFICATION);
    verifyNoInteractions(this.firmwareImageIdentifierCachingRepository);
    verifyNoInteractions(this.updateFirmwareCommandExecutor);
  }

  @Test
  void updateFirmwareWhenFirmwareFileNotInCacheAndNotOnStore() throws OsgpException {

    final byte[] firmwareFile = FIRMWARE_IDENTIFICATION.getBytes();

    when(this.firmwareFileCachingRepository.isAvailable(FIRMWARE_IDENTIFICATION)).thenReturn(false);
    when(this.firmwareFileStoreService.readFirmwareFile(FIRMWARE_IDENTIFICATION)).thenReturn(null);

    assertThatExceptionOfType(ProtocolAdapterException.class)
        .isThrownBy(
            () -> {
              this.firmwareService.updateFirmware(
                  this.dlmsConnectionManagerMock,
                  this.dlmsDeviceMock,
                  updateFirmwareRequestDto,
                  messageMetadata);
            })
        .withMessageContainingAll(FIRMWARE_IDENTIFICATION);

    verify(this.firmwareFileStoreService).readFirmwareFile(FIRMWARE_IDENTIFICATION);
    verify(this.firmwareFileCachingRepository, never())
        .store(FIRMWARE_IDENTIFICATION, firmwareFile);
    verify(this.firmwareFileCachingRepository, never()).retrieve(FIRMWARE_IDENTIFICATION);
    verifyNoInteractions(this.firmwareImageIdentifierCachingRepository);
    verifyNoInteractions(this.updateFirmwareCommandExecutor);
  }

  @Test
  void updateFirmwareWhenStoreThrowsException() throws OsgpException {

    final byte[] firmwareFile = FIRMWARE_IDENTIFICATION.getBytes();
    final String storeExceptionMessage = "firmware file store failed!";

    when(this.firmwareFileCachingRepository.isAvailable(FIRMWARE_IDENTIFICATION)).thenReturn(false);
    when(this.firmwareFileStoreService.readFirmwareFile(FIRMWARE_IDENTIFICATION))
        .thenThrow(new ProtocolAdapterException(storeExceptionMessage));

    assertThatExceptionOfType(ProtocolAdapterException.class)
        .isThrownBy(
            () -> {
              this.firmwareService.updateFirmware(
                  this.dlmsConnectionManagerMock,
                  this.dlmsDeviceMock,
                  updateFirmwareRequestDto,
                  messageMetadata);
            })
        .withMessage(storeExceptionMessage);

    verify(this.firmwareFileCachingRepository, never())
        .store(FIRMWARE_IDENTIFICATION, firmwareFile);
    verify(this.firmwareFileCachingRepository, never()).retrieve(FIRMWARE_IDENTIFICATION);
    verifyNoInteractions(this.firmwareImageIdentifierCachingRepository);
    verifyNoInteractions(this.updateFirmwareCommandExecutor);
  }
}
