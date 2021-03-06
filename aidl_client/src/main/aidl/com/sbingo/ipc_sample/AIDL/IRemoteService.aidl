// IRemoteService.aidl
package com.sbingo.ipc_sample.AIDL;

// Declare any non-default types here with import statements

interface IRemoteService {

    /** Request the process ID of this service, to do evil things with it. */
    int getPid();

    int add(int a, int b);

    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

}
