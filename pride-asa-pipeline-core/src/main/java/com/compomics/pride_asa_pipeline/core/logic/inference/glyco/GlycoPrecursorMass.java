package com.compomics.pride_asa_pipeline.core.logic.inference.glyco;

/**
 * An enum for known glycan related precursor mz peaks (not masses)
 * 
 * @author Kenneth Verheggen
 */
public enum GlycoPrecursorMass {
    Alditol(getAlditolMasses()),
    PyridylAmine(getPyridilAminoMasses()),
    Unlabelled(getUnlabelledGlycoMasses());
    private final double[] mzValues;

    private GlycoPrecursorMass(double[] mzValues) {
        this.mzValues = mzValues;
    }

    public double[] getMzValues() {
        return mzValues;
    }

    public static double[] getPyridilAminoMasses() {
        return new double[]{
            1011.4,
            1157.4,
            1214.4,
            1214.5,
            1335.5,
            1360.5,
            1376.5,
            1376.6,
            1417.6,
            1497.5,
            1497.6,
            1522.6,
            1579.6,
            1579.7,
            1620.6,
            1620.7,
            1620.8,
            1659.6,
            1725.7,
            1741.7,
            1766.7,
            1782.7,
            1821.7,
            1823.8,
            1863.7,
            1887.7,
            1928.8,
            1944.7,
            1944.8,
            1969.8,
            1983.8,
            1985.5,
            1985.7,
            1985.8,
            2009.8,
            2032.7,
            2032.8,
            2033.7,
            2033.8,
            2033.9,
            2090.8,
            2106.7,
            2106.8,
            2131.8,
            2131.9,
            2147.8,
            2229.9,
            2252.8,
            2252.9,
            2293.9,
            2309.9,
            2450,
            2455.9,
            2471.9,
            2472,
            2553.9,
            2617.9,
            3122.1};
    }

    public static double[] getAlditolMasses() {
        return new double[]{
            449,
            551.2,
            554.2,
            611.2,
            713.2,
            716.2,
            754.3,
            757.2,
            757.3,
            773.2,
            859.3,
            916.3,
            919.3,
            960.3,
            960.4,
            976.4,
            1021.4,
            1062.4,
            1078.4,
            1081.4,
            1119.4,
            1122.4,
            1224.4,
            1265.5,
            1281.5,
            1284.4,
            1284.5,
            1325.5,
            1341.5,
            1706.6,
            2071.8,
            2436.8};
    }

    public static double[] getUnlabelledGlycoMasses() {
        return new double[]{
            364,
            364.3,
            511.3,
            511.59,
            527.1,
            527.2,
            527.3,
            552.2,
            552.22,
            552.25,
            552.9,
            568.2,
            632.3,
            632.4,
            656.5,
            698.3,
            698.5,
            714.2,
            730.3,
            771.3,
            819.1,
            819.3,
            843.4,
            843.5,
            851.4,
            860.3,
            876.3,
            876.4,
            892.4,
            933.5,
            933.6,
            1054.4,
            1095.4,
            1095.8,
            1216.7,
            1339.6,
            1485.6,
            1533.6,
            1542.6,
            1581.8,
            1663.6,
            1746,
            1809.8,
            1866.7,
            1866.75,
            2029.1,
            2076.9,
            2122.9,
            2175.5
        };
    }
}
