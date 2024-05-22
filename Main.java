public class Main {

    public static void main(String[] args) {
            if (args.length != 1) {
                System.err.println("Usage: java Main <input_file>");
                System.exit(1);
            }

            DirectedGraphLab.main(args);
        }
    }

//修改