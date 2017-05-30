package com.vladislavkarpman.realmsyncingissue.RealmModel;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by vladislavkarpman on 5/29/17.
 */
public class Manufacture extends RealmObject {
    @Required
    String manufactureName;

    @Required
    String manufactureLocation;
}
