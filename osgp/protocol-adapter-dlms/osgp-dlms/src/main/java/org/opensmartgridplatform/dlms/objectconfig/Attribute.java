// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.dlms.objectconfig;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.opensmartgridplatform.dlms.interfaceclass.attribute.AttributeType;

@Getter
@NoArgsConstructor
public class Attribute {
  private int id;
  private String description;
  private String note;
  private DlmsDataType datatype;
  private AttributeType attributetype;
  private ValueType valuetype;
  private String rawValue;
  private String value;
  private ValueBasedOnModel valuebasedonmodel;
  private AccessType access;

  public Attribute(
      final int id,
      final String description,
      final String note,
      final DlmsDataType datatype,
      final ValueType valuetype,
      final String value,
      final ValueBasedOnModel valuebasedonmodel,
      final AccessType access) {
    this.id = id;
    this.description = description;
    this.note = note;
    this.datatype = datatype;
    this.valuetype = valuetype;
    this.value = value;
    this.valuebasedonmodel = valuebasedonmodel;
    this.access = access;
  }

  public Attribute(
      final int id,
      final String description,
      final String note,
      final DlmsDataType datatype,
      final AttributeType attributetype,
      final ValueType valuetype,
      final String value,
      final String rawValue,
      final ValueBasedOnModel valuebasedonmodel,
      final AccessType access) {
    this.id = id;
    this.description = description;
    this.note = note;
    this.datatype = datatype;
    this.attributetype = attributetype;
    this.valuetype = valuetype;
    this.value = value;
    this.rawValue = rawValue;
    this.valuebasedonmodel = valuebasedonmodel;
    this.access = access;
  }

  public Attribute copy() {
    return new Attribute(
        this.id,
        this.description,
        this.note,
        this.datatype,
        this.attributetype,
        this.valuetype,
        this.value,
        this.rawValue,
        this.valuebasedonmodel == null ? null : this.valuebasedonmodel.copy(),
        this.access);
  }

  public Attribute copyWithNewValue(final String newValue) {
    final Attribute newAttribute = this.copy();
    newAttribute.value = newValue;
    return newAttribute;
  }

  public Attribute copyWithNewValueAndType(final String newValue, final ValueType newValueType) {
    final Attribute newAttribute = this.copy();
    newAttribute.value = newValue;
    newAttribute.valuetype = newValueType;
    return newAttribute;
  }
}
