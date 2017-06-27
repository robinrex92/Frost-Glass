package in.robinrex.frostglass;

/**
 * Custom exception class used by FrostGlass.
 *
 * @author Robin Rex G.
 */

public class FGException extends IllegalStateException {

    public FGException(String message) {
        super(message);
    }
}
