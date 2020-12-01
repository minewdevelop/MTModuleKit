package com.minew.mtmoduledemo;

import com.minew.modulekit.MTModule;

public class MTOperate {
    private static final MTOperate ourInstance = new MTOperate();

    public static MTOperate getInstance() {
        return ourInstance;
    }

    private MTOperate() {}


    private MTModule mtModule;

    public MTModule getMtModule() {
        return mtModule;
    }

    public void setMtModule(MTModule mtModule) {
        this.mtModule = mtModule;
    }
}
