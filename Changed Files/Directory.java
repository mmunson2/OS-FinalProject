public class Directory {
    private static int maxChars = 30; // max characters of each file name

    // Directory entries
    private int fsize[]; // each element stores a different file size.
    private char fnames[][]; // each element stores a different file name.

    public Directory(int maxInumber) { // directory constructor
        fsize = new int[maxInumber]; // maxInumber = max files
        for (int i = 0; i < maxInumber; i++)
            fsize[i] = 0; // all file size initialized to 0
        fnames = new char[maxInumber][maxChars];
        String root = "/"; // entry(inode) 0 is "/"
        fsize[0] = root.length(); // fsize[0] is the size of "/".
        root.getChars(0, fsize[0], fnames[0], 0); // fnames[0] includes "/"
    }

    public void bytes2directory(byte data[]) {
        // assumes data[] received directory information from disk
        // initializes the Directory instance with this data[]

        int seek = 0;

        for (int i = 0; i < this.fsize.length; i++) {
            this.fsize[i] = SysLib.bytes2int(data, seek);

            seek += (Integer.SIZE / Byte.SIZE);
        }

        int maxLength = maxChars * Character.SIZE / Byte.SIZE;


        for (int i = 0; i < this.fnames.length; i++) {
            String fileName = new String(data, seek, maxLength);
            fileName.getChars(0, this.fsize[i], this.fnames[i], 0);

            seek += maxLength;
        }
    }

    public byte[] directory2bytes() {
        // converts and return Directory information into a plain byte array
        // this byte array will be written back to disk
        // note: only meaningfull directory information should be converted
        // into bytes.

        byte[] buffer = new byte[this.fsize.length * (Integer.SIZE / Byte.SIZE) + this.fnames.length * (Character.SIZE / Byte.SIZE) * maxChars];

        int seek = 0;

        for (int i = 0; i < this.fsize.length; i++) {
            SysLib.int2bytes(this.fsize[i], buffer, seek);
            seek += (Integer.SIZE / Byte.SIZE);
        }

        int maxLength = maxChars * Character.SIZE / Byte.SIZE;


        for (int i = 0; i < this.fnames.length; i++) {
            String fileName = new String(this.fnames[i], 0, this.fsize[i]);
            byte[] fNameBytes = fileName.getBytes();

            System.arraycopy(fileName, 0, buffer, seek, fNameBytes.length);

            seek += maxLength;
        }

        return buffer;
    }

    public short ialloc(String fileName) {
        // filename is the one of a file to be created.
        // allocates a new inode number for this filename
        for (int i = 1; i < this.fsize.length; ++i) {
            if (this.fsize[i] == 0) {
                this.fsize[i] = fileName.length() < maxChars ? fileName.length() : maxChars;
                fileName.getChars(0, this.fsize[i], this.fnames[i], 0);
                return (short) i;
            }
        }

        return -1;
    }
        public boolean ifree(short iNumber) {
            // deallocates this inumber (inode number)
            // the corresponding file will be deleted.
            if (this.fsize[iNumber] <= 0) {
                return false;
            }
            this.fsize[iNumber] = 0;
            return true;
        }

        /*
        public short namei(String fileName)
        {
            // returns the inumber corresponding to this filename
            for (int i = 0; i < this.fsize.length; i++)
            {
                String fileNameCheck = new String(this.fnames[i], 0, this.fsize[i]);
                if (fileName.equals(fileNameCheck))
                    return (short) i;
            }
            return -1;
        }
        */

        public short namei(String var1) {
            for(short var2 = 0; var2 < this.fsize.length; ++var2) {
                if (this.fsize[var2] == var1.length()) {
                    String var3 = new String(this.fnames[var2], 0, this.fsize[var2]);
                    if (var1.compareTo(var3) == 0) {
                        return var2;
                    }
                }
            }

            return -1;
        }

    }