package com.vladislavkarpman.realmsyncingissue.RealmModel;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by vladislavkarpman on 5/29/17.
 */
public class Car extends RealmObject {

    @PrimaryKey
    int carId;

    Manufacture carManufacture;

    RealmList<Owner> carOwners;

    @Required
    String carYear;
}
