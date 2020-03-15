/*******************************************************************************
 * Directory Class
 *
 * A directory that can hold numerous files
 ******************************************************************************/
public class Directory
{
    private static int maxChars = 30; // max characters of each file name

    // Directory entries
    private int fsize[]; // each element stores a different file size.
    private char fnames[][]; // each element stores a different file name.

    /***************************************************************************
     * Directory Constructor
     *
     * Takes in the maximum number of files
     **************************************************************************/
    Directory(int maxInumber)
    {
        this.fsize = new int[maxInumber]; // maxInumber = max files

        for (int i = 0; i < maxInumber; i++)
        {
            this.fsize[i] = 0; // all file size initialized to 0
        }

        fnames = new char[maxInumber][maxChars];
        String root = "/"; // entry(inode) 0 is "/"
        this.fsize[0] = root.length(); // fsize[0] is the size of "/".
        // fnames[0] includes "/"
        root.getChars(0, this.fsize[0], fnames[0], 0);
    }

    /***************************************************************************
     * bytes2directory
     *
     * • assumes data[] received directory information from disk
     * • initializes the Directory instance with this data[]
     **************************************************************************/
    void bytes2directory(byte data[])
    {
        int seek = 0;

        for (int i = 0; i < this.fsize.length; i++)
        {
            this.fsize[i] = SysLib.bytes2int(data, seek);

            seek += (Integer.SIZE / Byte.SIZE);
        }

        int maxLength = maxChars * Character.SIZE / Byte.SIZE;

        for (int i = 0; i < this.fnames.length; i++)
        {
            String fileName = new String(data, seek, maxLength);
            fileName.getChars(
                    0, this.fsize[i], this.fnames[i], 0);

            seek += maxLength;
        }
    }

    /***************************************************************************
     * directory2bytes
     *
     * • converts and return Directory information into a plain byte array
     * • this byte array will be written back to disk
     * • note: only meaningful directory information should be converted
     * into bytes.
     **************************************************************************/
    public byte[] directory2bytes()
    {
        byte[] buffer = new byte[this.fsize.length * (Integer.SIZE / Byte.SIZE)
                + this.fnames.length * (Character.SIZE / Byte.SIZE) * maxChars];

        int seek = 0;

        for (int i : this.fsize)
        {
            SysLib.int2bytes(this.fsize[i], buffer, seek);
            seek += (Integer.SIZE / Byte.SIZE);
        }

        int maxLength = maxChars * Character.SIZE / Byte.SIZE;


        for (int i = 0; i < this.fnames.length; i++)
        {
            String fileName = new String(
                    this.fnames[i], 0, this.fsize[i]);
            byte[] fNameBytes = fileName.getBytes();

            System.arraycopy(
                    fileName, 0, buffer, seek, fNameBytes.length);

            seek += maxLength;
        }

        return buffer;
    }

    /***************************************************************************
     * ialloc
     *
     * • filename is the one of a file to be created.
     * • allocates a new inode number for this filename
     **************************************************************************/
    short ialloc(String fileName)
    {
        for (int i = 1; i < this.fsize.length; ++i)
        {
            if (this.fsize[i] == 0)
            {
                if(fileName.length() < maxChars)
                {
                    this.fsize[i] = fileName.length();
                }
                else
                {
                    this.fsize[i] = maxChars;
                }

                fileName.getChars(
                        0, this.fsize[i], this.fnames[i], 0);
                return (short) i;
            }
        }
        return -1;
    }


    /***************************************************************************
     * ifree
     *
     * • deallocates this inumber (inode number)
     * • the corresponding file will be deleted.
     **************************************************************************/
    boolean ifree(short iNumber)
    {
        if (this.fsize[iNumber] <= 0)
        {
            return false;
        }
        this.fsize[iNumber] = 0;
        return true;
    }

    /***************************************************************************
     * namei
     *
     * Checks if a name exists in the directory
     **************************************************************************/
    short namei(String fileName)
    {
        for(short i = 0; i < this.fsize.length; ++i)
        {
            if (this.fsize[i] == fileName.length())
            {
                String compareFile = new String(
                        this.fnames[i], 0, this.fsize[i]);
                if (fileName.equals(compareFile))
                {
                    return i;
                }
            }
        }
        return -1;
    }

}