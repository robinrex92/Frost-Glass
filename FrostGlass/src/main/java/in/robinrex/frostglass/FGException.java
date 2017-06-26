package in.robinrex.frostglass;

/**
 * Custom exception class used by this library.
 *
 * @author Robin Rex G.
 */

public class FGException extends IllegalStateException {

    public FGException(String message) {
        super(message);
    }
}
