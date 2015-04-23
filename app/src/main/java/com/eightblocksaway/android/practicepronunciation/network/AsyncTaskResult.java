package com.eightblocksaway.android.practicepronunciation.network;

import org.jetbrains.annotations.NotNull;

public class AsyncTaskResult<T> {
    private T result;
    private Exception error;



    public T getResult() {
        return result;
    }
    public Exception getError() {
        return error;
    }


    public AsyncTaskResult(@NotNull T result) {
        super();
        this.result = result;
    }


    public AsyncTaskResult(@NotNull Exception error) {
        super();
        this.error = error;
    }

    public boolean wasSuccessfull(){
        return error == null;
    }
}