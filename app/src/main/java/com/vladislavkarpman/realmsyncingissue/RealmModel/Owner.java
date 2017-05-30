package com.vladislavkarpman.realmsyncingissue.RealmModel;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by vladislavkarpman on 5/29/17.
 */

public class Owner extends RealmObject {

    @PrimaryKey
    private int ownerId;

    RealmList<Car> ownerCars;

    @Required
    String ownerName;

    @Required
    String ownerYear;
}
