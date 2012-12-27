package experimentalcode.franz;

public class Simplex3Result extends AbstractResult {

    @Override
    public void setUnits(String... unitStrings) {
        if (unitStrings.length != 3) {
            throw new IllegalArgumentException("only 3 units allowed");
        }
        units.clear();
        for (String s : unitStrings) {
            units.add(s);
        }
    }
}
