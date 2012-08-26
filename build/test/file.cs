// ==++== 中文ok——
//
//   Copyright (c) Microsoft Corporation.  All rights reserved.
//
// ==--==
//============================================================
//
// Class:  File
//
// Purpose: A collection of methods for manipulating Files.
//
//===========================================================

using System;
using System.Runtime.InteropServices;
using System.Text;
using FileSystem.Utils;
using Microsoft.Singularity.Directory;

namespace System.IO
{
    // Class for creating FileStream objects, and some basic file management
    // routines such as Delete, etc.
    //| <include file='doc\File.uex' path='docs/doc[@for="File"]/*' />
    public sealed class File
    {
        private const int GetFileExInfoStandard = 0;

        private File()
        {
        }


        //| <include file='doc\File.uex' path='docs/doc[@for="File.OpenText"]/*' />
        public static StreamReader OpenText(String path)
        {
            if (path == null)
                throw new ArgumentNullException("path");
            return new StreamReader(path);
        }

        //| <include file='doc\File.uex' path='docs/doc[@for="File.CreateText"]/*' />
        public static StreamWriter CreateText(String path)
        {
            if (path == null)
                throw new ArgumentNullException("path");
            return new StreamWriter(path,false);
        }

        //| <include file='doc\File.uex' path='docs/doc[@for="File.AppendText"]/*' />
        public static StreamWriter AppendText(String path)
        {
            if (path == null)
                throw new ArgumentNullException("path");
            return new StreamWriter(path,true);
        }


        // Copies an existing file to a new file. An exception is raised if the
        // destination file already exists. Use the
        // Copy(String, String, boolean) method to allow
        // overwriting an existing file.
        //
        //| <include file='doc\File.uex' path='docs/doc[@for="File.Copy"]/*' />
        public static void Copy(String sourceFileName, String destFileName) {
            Copy(sourceFileName, destFileName, false);
        }

        // Copies an existing file to a new file. If overwrite is
        // false, then an IOException is thrown if the destination file
        // already exists.  If overwrite is true, the file is
        // overwritten.
        //
        //| <include file='doc\File.uex' path='docs/doc[@for="File.Copy1"]/*' />
        public static void Copy(String sourceFileName, String destFileName, bool overwrite) {
            InternalCopy(sourceFileName, destFileName,overwrite);
        }

        //| <include file='doc\File.uex' path='docs/doc[@for="File.InternalCopy"]/*' />
        /// <devdoc>
        ///    Note: This returns the fully qualified name of the destination file.
        /// </devdoc>
        internal static String! InternalCopy(String sourceFileName, String destFileName, bool overwrite) {
            if (sourceFileName == null || destFileName == null)
                throw new ArgumentNullException((sourceFileName==null ? "sourceFileName" : "destFileName"), "ArgumentNull_FileName");
            if (sourceFileName.Length == 0 || destFileName.Length == 0)
                throw new ArgumentException("Argument_EmptyFileName", (sourceFileName.Length==0 ? "sourceFileName" : "destFileName"));

            String fullSourceFileName = Path.GetFullPathInternal(sourceFileName);
            String fullDestFileName = Path.GetFullPathInternal(destFileName);

            bool r = Native.CopyFile(fullSourceFileName, fullDestFileName, !overwrite);
            if (!r) {
                __Error.WinIOError(0, destFileName);
            }

            return fullDestFileName;
        }


        // Creates a file in a particular path.  If the file exists, it is replaced.
        // The file is opened with ReadWrite accessand cannot be opened by another
        // application until it has been closed.  An IOException is thrown if the
        // directory specified doesn't exist.
        //
        // Your application must have Create, Read, and Write permissions to
        // the file.
        //
        //| <include file='doc\File.uex' path='docs/doc[@for="File.Create"]/*' />
        public static FileStream Create(String path) {
            return Create(path, FileStream.DefaultBufferSize);
        }

        // Creates a file in a particular path.  If the file exists, it is replaced.
        // The file is opened with ReadWrite access and cannot be opened by another
        // application until it has been closed.  An IOException is thrown if the
        // directory specified doesn't exist.
        //
        // Your application must have Create, Read, and Write permissions to
        // the file.
        //
        //| <include file='doc\File.uex' path='docs/doc[@for="File.Create1"]/*' />
        public static FileStream Create(String path, int bufferSize) {
            return new FileStream(path, FileMode.Create, FileAccess.ReadWrite,
                                  FileShare.None, bufferSize);
        }

        // Deletes a file. The file specified by the designated path is deleted.
        // If the file does not exist, Delete succeeds without throwing
        // an exception.
        //
        // On NT, Delete will fail for a file that is open for normal I/O
        // or a file that is memory mapped.
        //
        // Your application must have Delete permission to the target file.
        //
        //| <include file='doc\File.uex' path='docs/doc[@for="File.Delete"]/*' />
        public static void Delete(String path) {
            if (path == null)
                throw new ArgumentNullException("path");

            String fullPath = Path.GetFullPathInternal(path);
            ErrorCode error;
            bool r = Native.DeleteFile(fullPath, out error);
            if (!r) {
                //int err =  __Error.ErrorCodeToWin32Error(error);
                //__Error.WinIOError(err, path);
                __Error.SingularityIOError(error,path);
            }
        }

        // Tests if a file exists. The result is true if the file
        // given by the specified path exists; otherwise, the result is
        // false.  Note that if path describes a directory,
        // Exists will return true.
        //
        // Your application must have Read permission for the target directory.
        //
        //| <include file='doc\File.uex' path='docs/doc[@for="File.Exists"]/*' />
        public static bool Exists(String! path)
        {
            DirectoryServiceContract.Imp dsRoot = DirectoryService.NewClientEndpoint();
            if (dsRoot == null) throw new Exception("No directory service endpoint.");
            bool ok = Exists(dsRoot, path);
            delete dsRoot;
            return ok;
        }

        public static bool Exists(DirectoryServiceContract.Imp:Ready! dsRoot, String! path) {
        try {
                path = Path.GetFullPathInternal(path);
                if (path == null)
                    return false;
                if (path.Length == 0)
                    return false;
                bool exists = FileUtils.FileExists(dsRoot, (!)path);

#if false // uncomment to debug file access.
                Console.WriteLine("FileExists({0}) = {1}", path, exists);
#endif
                return exists;
            }
            catch (ArgumentException) {

            }
            catch (NotSupportedException) {} // To deal with the fact that security can now throw this on :
            catch (IOException) {

            }
            return false;
        }

         internal static bool InternalExists(String path) {
            Native.FILE_ATTRIBUTE_DATA data = new Native.FILE_ATTRIBUTE_DATA();
            int dataInitialised = FillAttributeInfo(path,ref data);
            if (dataInitialised != 0)
                return false;

            return (data.fileAttributes  & Native.FILE_ATTRIBUTE_DIRECTORY) == 0;
        }


        //| <include file='doc\File.uex' path='docs/doc[@for="File.Open"]/*' />
        public static FileStream Open(String path,FileMode mode) {
            return Open(path, mode, (mode == FileMode.Append ? FileAccess.Write : FileAccess.ReadWrite), FileShare.None);
        }

        //| <include file='doc\File.uex' path='docs/doc[@for="File.Open1"]/*' />
        public static FileStream Open(String path,FileMode mode, FileAccess access) {
            return Open(path,mode, access, FileShare.None);
        }

        //| <include file='doc\File.uex' path='docs/doc[@for="File.Open2"]/*' />
        public static FileStream Open(String path, FileMode mode, FileAccess access, FileShare share) {
            return new FileStream(path, mode, access, share);
        }

        //| <include file='doc\File.uex' path='docs/doc[@for="File.SetCreationTime"]/*' />
        public static void SetCreationTime(String path, DateTime creationTime)
        {
            SetCreationTimeUtc(path, creationTime.ToUniversalTime());
        }

        //| <include file='doc\File.uex' path='docs/doc[@for="File.SetCreationTimeUtc"]/*' />
        public static void SetCreationTimeUtc(String path, DateTime creationTimeUtc)
        {
            IntPtr handle = IntPtr.Zero;
            FileStream fs = OpenFile(path, FileAccess.Write, ref handle);

            bool r = Native.SetFileTime(handle,  new long[] {creationTimeUtc.ToFileTimeUtc()}, null, null);
            if (!r) {
                 fs.Close();
                __Error.WinIOError(0, path);
            }
            fs.Close();
        }

        //| <include file='doc\File.uex' path='docs/doc[@for="File.GetCreationTimeUtc"]/*' />
        public static DateTime GetCreationTimeUtc(String! path)
        {
            String fullPath = Path.GetFullPathInternal(path);

            Native.FILE_ATTRIBUTE_DATA data = new Native.FILE_ATTRIBUTE_DATA();
            int dataInitialised = FillAttributeInfo(fullPath,ref data);
            if (dataInitialised != 0)
                __Error.WinIOError(dataInitialised, path);

            if (data.fileAttributes == -1)
                throw new IOException(String.Format("IO.PathNotFound_Path", path));

            return DateTime.FromFileTimeUtc(data.ftCreationTime);
        }

        //| <include file='doc\File.uex' path='docs/doc[@for="File.SetLastAccessTimeUtc"]/*' />
        public static void SetLastAccessTimeUtc(String path, DateTime lastAccessTimeUtc)
        {
            IntPtr handle = IntPtr.Zero;
            FileStream fs = OpenFile(path, FileAccess.Write, ref handle);

            bool r = Native.SetFileTime(handle, null, new long[] {lastAccessTimeUtc.ToFileTimeUtc()},  null);
            if (!r) {
                 fs.Close();
                __Error.WinIOError(0, path);
            }

            fs.Close();
        }

        //| <include file='doc\File.uex' path='docs/doc[@for="File.GetLastAccessTimeUtc"]/*' />
        public static DateTime GetLastAccessTimeUtc(String! path)
        {
            String fullPath = Path.GetFullPathInternal(path);

            Native.FILE_ATTRIBUTE_DATA data = new Native.FILE_ATTRIBUTE_DATA();
            int dataInitialised = FillAttributeInfo(fullPath,ref data);
            if (dataInitialised != 0)
                __Error.WinIOError(dataInitialised, path);

            if (data.fileAttributes == -1)
                throw new IOException(String.Format("IO.PathNotFound_Path", path));

            return DateTime.FromFileTimeUtc(data.ftLastAccessTime);
        }

        //| <include file='doc\File.uex' path='docs/doc[@for="File.SetLastWriteTimeUtc"]/*' />
        public static void SetLastWriteTimeUtc(String path, DateTime lastWriteTimeUtc)
        {
            IntPtr handle = IntPtr.Zero;
            FileStream fs = OpenFile(path, FileAccess.Write, ref handle);

            bool r = Native.SetFileTime(handle, null, null, new long[] {lastWriteTimeUtc.ToFileTimeUtc()});
            if (!r) {
                 fs.Close();
                __Error.WinIOError(0, path);
            }
            fs.Close();
        }

        public static DateTime GetLastWriteTime(String! path)
        {
            //TODO: FIXFIX need to convert to local time some day
            return GetLastWriteTimeUtc(path);
        }

        //| <include file='doc\File.uex' path='docs/doc[@for="File.GetLastWriteTimeUtc"]/*' />
        public static DateTime GetLastWriteTimeUtc(String! path)
        {
            String fullPath = Path.GetFullPathInternal(path);

            Native.FILE_ATTRIBUTE_DATA data = new Native.FILE_ATTRIBUTE_DATA();
            int dataInitialised = FillAttributeInfo(fullPath,ref data);
            if (dataInitialised != 0)
                __Error.WinIOError(dataInitialised, path);

            if (data.fileAttributes == -1)
                throw new IOException(String.Format("IO.PathNotFound_Path", path));

            return DateTime.FromFileTimeUtc(data.ftLastWriteTime);
        }

        //| <include file='doc\File.uex' path='docs/doc[@for="File.GetAttributes"]/*' />
        public static FileAttributes GetAttributes(String! path)
        {
            String fullPath = Path.GetFullPathInternal(path);

            Native.FILE_ATTRIBUTE_DATA data = new Native.FILE_ATTRIBUTE_DATA();
            int dataInitialised = FillAttributeInfo(fullPath,ref data);
            if (dataInitialised != 0)
                __Error.WinIOError(dataInitialised, path);

            if (data.fileAttributes == -1)
                __Error.WinIOError(Native.ERROR_FILE_NOT_FOUND, path);

            return (FileAttributes) data.fileAttributes;
        }

        //| <include file='doc\File.uex' path='docs/doc[@for="File.SetAttributes"]/*' />
        public static void SetAttributes(String! path,FileAttributes fileAttributes)
        {
            String fullPath = Path.GetFullPathInternal(path);
            bool r = Native.SetFileAttributes(fullPath, (int) fileAttributes);
            if (!r) {
                 __Error.WinIOError(0, path);
            }
        }

        //| <include file='doc\File.uex' path='docs/doc[@for="File.OpenRead"]/*' />
        public static FileStream OpenRead(String path) {
            return new FileStream(path, FileMode.Open, FileAccess.Read, FileShare.Read);
        }


        //| <include file='doc\File.uex' path='docs/doc[@for="File.OpenWrite"]/*' />
        public static FileStream OpenWrite(String path) {
            return new FileStream(path, FileMode.OpenOrCreate,
                                  FileAccess.Write, FileShare.None);
        }



        // Moves a specified file to a new location and potentially a new file name.
        // This method does work across volumes.
        //
        //| <include file='doc\File.uex' path='docs/doc[@for="File.Move"]/*' />
        public static void Move(String sourceFileName, String destFileName) {
            if (sourceFileName == null || destFileName == null)
                throw new ArgumentNullException((sourceFileName==null ? "sourceFileName" : "destFileName"), "ArgumentNull_FileName");
            if (sourceFileName.Length == 0 || destFileName.Length == 0)
                throw new ArgumentException("Argument_EmptyFileName", (sourceFileName.Length==0 ? "sourceFileName" : "destFileName"));

            String fullSourceFileName = Path.GetFullPathInternal(sourceFileName);
            String fullDestFileName = Path.GetFullPathInternal(destFileName);

            if (!InternalExists(fullSourceFileName))
                __Error.WinIOError(Native.ERROR_FILE_NOT_FOUND,sourceFileName);

            if (!Native.MoveFile(fullSourceFileName, fullDestFileName)) {
                __Error.WinIOError(0, destFileName);
            }
        }

        internal static int FillAttributeInfo(String path, ref Native.FILE_ATTRIBUTE_DATA data)
        {
            int dataInitialised = 0;

            // For floppy drives, normally the OS will pop up a dialog saying
            // there is no disk in drive A:, please insert one.  We don't want that.
            bool success = Native.GetFileAttributesEx(path, GetFileExInfoStandard, ref data);

            if (!success) {
                data.fileAttributes = -1;
                dataInitialised = 0;
            }
            return dataInitialised;
        }

        private static FileStream! OpenFile(String path, FileAccess access, ref IntPtr handle)
        {
            return new FileStream(path, FileMode.Open, access, FileShare.ReadWrite, 1);
        }


         // Defined in WinError.h
        private const int ERROR_INVALID_PARAMETER = 87;
        private const int ERROR_ACCESS_DENIED = 0x5;

    }
}

//END