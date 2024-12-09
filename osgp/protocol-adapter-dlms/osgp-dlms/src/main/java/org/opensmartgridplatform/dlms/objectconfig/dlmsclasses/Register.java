// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.dlms.objectconfig.dlmsclasses;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.opensmartgridplatform.dlms.interfaceclass.attribute.RegisterAttribute;
import org.opensmartgridplatform.dlms.objectconfig.Attribute;
import org.opensmartgridplatform.dlms.objectconfig.MeterType;
import org.opensmartgridplatform.dlms.objectconfig.ObjectProperty;
import org.opensmartgridplatform.dlms.objectconfig.ValueType;

@Getter
@NoArgsConstructor
public class Register extends Data {

  public Register(
      final String tag,
      final String description,
      final int classId,
      final int version,
      final String obis,
      final String group,
      final String note,
      final List<MeterType> meterTypes,
      final Map<ObjectProperty, Object> properties,
      final List<Attribute> attributes) {
    super(
        tag, description, classId, version, obis, group, note, meterTypes, properties, attributes);
  }

  public boolean needsScalerUnitFromMeter() {
    final Attribute scalerUnitAttribute =
        this.getAttribute(RegisterAttribute.SCALER_UNIT.attributeId());

    if (scalerUnitAttribute == null
        || scalerUnitAttribute.getValue() == null
        || scalerUnitAttribute.getValue().isEmpty()) {
      return true;
    }

    return scalerUnitAttribute.getValuetype() == ValueType.DYNAMIC;
  }

  public String getScalerUnit() {
    final Attribute scalerUnitAttribute =
        this.getAttribute(RegisterAttribute.SCALER_UNIT.attributeId());

    return scalerUnitAttribute.getValue();
  }

  @Override
  public Register copy() {
    return new Register(
        this.tag,
        this.description,
        this.classId,
        this.version,
        this.obis,
        this.group,
        this.note,
        new ArrayList<>(this.meterTypes),
        this.properties == null || this.properties.isEmpty()
            ? null
            : new EnumMap<>(this.properties),
        this.attributes.stream().map(Attribute::copy).toList());
  }

  @Override
  public Register copyWithNewObis(final String newObis) {
    final Register newRegister = this.copy();
    newRegister.obis = newObis;
    return newRegister;
  }

  @Override
  public Register copyWithNewAttribute(final Attribute newAttribute) {
    final Register newRegister = this.copy();

    // Remove attribute with same id as newAttribute
    final List<Attribute> copiedAttributes =
        newRegister.attributes.stream()
            .filter(attr -> attr.getId() != newAttribute.getId())
            .collect(Collectors.toList());
    copiedAttributes.add(newAttribute);

    // Add new attribute
    newRegister.attributes = copiedAttributes;

    return newRegister;
  }
}
