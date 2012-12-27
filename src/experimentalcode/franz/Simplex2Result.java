package experimentalcode.franz;

public class Simplex2Result extends AbstractResult {

    @Override
    public void setUnits(String... unitStrings) {
        if (unitStrings.length != 2) {
            throw new IllegalArgumentException("only 2 units allowed");
        }
        units.clear();
        for (String s : unitStrings) {
            units.add(s);
        }
    }
}
