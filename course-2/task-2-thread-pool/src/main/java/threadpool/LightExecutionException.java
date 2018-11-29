package threadpool;

public class LightExecutionException extends Exception {
    LightExecutionException(Throwable reason) {
        super(reason);
    }
}
