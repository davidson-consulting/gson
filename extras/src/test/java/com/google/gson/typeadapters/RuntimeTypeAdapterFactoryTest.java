/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.typeadapters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import junit.framework.TestCase;

public final class RuntimeTypeAdapterFactoryTest extends TestCase {

  public void testRuntimeTypeAdapter() {
    RuntimeTypeAdapterFactory<BillingInstrument> rta = RuntimeTypeAdapterFactory.of(
        BillingInstrument.class)
        .registerSubtype(CreditCard.class);
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(BillingInstrument.class, rta)
        .create();

    CreditCard original = new CreditCard("Jesse", 234);
    assertEquals("{\"type\":\"CreditCard\",\"cvv\":234,\"ownerName\":\"Jesse\"}",
        gson.toJson(original, BillingInstrument.class));
    BillingInstrument deserialized = gson.fromJson(
        "{type:'CreditCard',cvv:234,ownerName:'Jesse'}", BillingInstrument.class);
    assertEquals("Jesse", deserialized.ownerName);
    assertTrue(deserialized instanceof CreditCard);
  }

  public void testRuntimeTypeIsBaseType() {
    RuntimeTypeAdapterFactory<BillingInstrument> rta = RuntimeTypeAdapterFactory.of(
        BillingInstrument.class)
        .registerSubtype(BillingInstrument.class);
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(BillingInstrument.class, rta)
        .create();

    BillingInstrument original = new BillingInstrument("Jesse");
    assertEquals("{\"type\":\"BillingInstrument\",\"ownerName\":\"Jesse\"}",
        gson.toJson(original, BillingInstrument.class));
    BillingInstrument deserialized = gson.fromJson(
        "{type:'BillingInstrument',ownerName:'Jesse'}", BillingInstrument.class);
    assertEquals("Jesse", deserialized.ownerName);
  }

  public void testNullBaseType() {
    try {
      RuntimeTypeAdapterFactory.of(null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testNullTypeFieldName() {
    try {
      RuntimeTypeAdapterFactory.of(BillingInstrument.class, null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testNullSubtype() {
    RuntimeTypeAdapterFactory<BillingInstrument> rta = RuntimeTypeAdapterFactory.of(
        BillingInstrument.class);
    try {
      rta.registerSubtype(null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testNullLabel() {
    RuntimeTypeAdapterFactory<BillingInstrument> rta = RuntimeTypeAdapterFactory.of(
        BillingInstrument.class);
    try {
      rta.registerSubtype(CreditCard.class, null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testDuplicateSubtype() {
    RuntimeTypeAdapterFactory<BillingInstrument> rta = RuntimeTypeAdapterFactory.of(
        BillingInstrument.class);
    rta.registerSubtype(CreditCard.class, "CC");
    try {
      rta.registerSubtype(CreditCard.class, "Visa");
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testDuplicateLabel() {
    RuntimeTypeAdapterFactory<BillingInstrument> rta = RuntimeTypeAdapterFactory.of(
        BillingInstrument.class);
    rta.registerSubtype(CreditCard.class, "CC");
    try {
      rta.registerSubtype(BankTransfer.class, "CC");
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testDeserializeMissingTypeField() {
    Object billingAdapter = RuntimeTypeAdapterFactory.of(BillingInstrument.class)
        .registerSubtype(CreditCard.class);
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(BillingInstrument.class, billingAdapter)
        .create();
    try {
      gson.fromJson("{ownerName:'Jesse'}", BillingInstrument.class);
      fail();
    } catch (JsonParseException expected) {
    }
  }

  public void testDeserializeMissingSubtype() {
    Object billingAdapter = RuntimeTypeAdapterFactory.of(BillingInstrument.class)
        .registerSubtype(BankTransfer.class);
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(BillingInstrument.class, billingAdapter)
        .create();
    try {
      gson.fromJson("{type:'CreditCard',ownerName:'Jesse'}", BillingInstrument.class);
      fail();
    } catch (JsonParseException expected) {
    }
  }

  public void testSerializeMissingSubtype() {
    Object billingAdapter = RuntimeTypeAdapterFactory.of(BillingInstrument.class)
        .registerSubtype(BankTransfer.class);
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(BillingInstrument.class, billingAdapter)
        .create();
    try {
      gson.toJson(new CreditCard("Jesse", 456), BillingInstrument.class);
      fail();
    } catch (JsonParseException expected) {
    }
  }

  public void testSerializeCollidingTypeFieldName() {
    Object billingAdapter = RuntimeTypeAdapterFactory.of(BillingInstrument.class, "cvv")
        .registerSubtype(CreditCard.class);
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(BillingInstrument.class, billingAdapter)
        .create();
    try {
      gson.toJson(new CreditCard("Jesse", 456), BillingInstrument.class);
      fail();
    } catch (JsonParseException expected) {
    }
  }

  static class BillingInstrument {
    private final String ownerName;
    BillingInstrument(String ownerName) {
      this.ownerName = ownerName;
    }
  }

  static class CreditCard extends BillingInstrument {
    int cvv;
    CreditCard(String ownerName, int cvv) {
      super(ownerName);
      this.cvv = cvv;
    }
  }

  static class BankTransfer extends BillingInstrument {
    int bankAccount;
    BankTransfer(String ownerName, int bankAccount) {
      super(ownerName);
      this.bankAccount = bankAccount;
    }
  }
}
