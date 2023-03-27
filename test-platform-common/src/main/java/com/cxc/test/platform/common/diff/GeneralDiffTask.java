package com.cxc.test.platform.common.diff;

import com.cxc.test.platform.common.domain.diff.DiffDetail;

import java.util.concurrent.Callable;

public abstract class GeneralDiffTask implements Callable<DiffDetail> {

    @Override
    public abstract DiffDetail call() throws Exception;
}
