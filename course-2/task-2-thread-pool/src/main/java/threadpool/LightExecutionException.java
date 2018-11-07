package threadpool;

class LightExecutionException extends Exception {
    LightExecutionException(Throwable reason) {
        super(reason);
    }
}
