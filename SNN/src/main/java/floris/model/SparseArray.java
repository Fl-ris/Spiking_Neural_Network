package floris.model;

//import org.apache.spark.ml.linalg.Matrices;
//import org.apache.spark.ml.linalg.SparseMatrix;

public class SparseArray {
    public static void main(String[] args) {



    int numRows = 3;
    int numCols = 3;
    int[] colPtrs = {0, 2, 3, 6};  // Column pointers
    int[] rowIndices = {0, 2, 1, 0, 1, 2};  // Row indices
    double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};  // Non-zero values

//    SparseMatrix sparseMatrix = (SparseMatrix) Matrices.sparse(
//            numRows, numCols, colPtrs, rowIndices, values);
//
//    // Method 2: Direct constructor
//    SparseMatrix matrix = new SparseMatrix(
//            numRows, numCols, colPtrs, rowIndices, values
//    );
//
//    // Accessing elements
//    double element = matrix.apply(0, 2);  // Get element at row 0, col 2
//        System.out.println("Element at (0,2): " + element);  // Should print 4.0
//
//    // Get matrix properties
//        System.out.println("Num rows: " + matrix.numRows());
//        System.out.println("Num cols: " + matrix.numCols());
//        System.out.println("Num non-zeros: " + matrix.numNonzeros());

//    // Convert to dense matrix if needed
//    double[] denseArray = matrix.toArray();
//
//    // Print the matrix
//        System.out.println(matrix.toString());

}
}
