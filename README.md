# RealmSyncingIssue

Test project that simulates Realm.io Sync issue.

I come to a conclusion that something goes wrong with Realm Sync if i set ChangeListeners on tables which have inverse connections, in my example it's Owner and Car and at the same time i have pretty much data in those tables Realm Syncing stop working at all. Check out the project to dig a bit deeper to the problem
