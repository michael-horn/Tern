//===========================================================
// FILE:      launcher.cpp
//===========================================================
#pragma resource "bin/resources.res"
#include <windows.h>
#include <stdio.h>
#include <stdlib.h>

#define SPLASH_SCREEN     700
#define IDI_ICON          701
#define IDI_ICON_SM       702

#define JRE_REG   "SOFTWARE\\JavaSoft\\Java Runtime Environment"
#define JRE_HKEY  "JavaHome"


//-------------------------------------------------------------------
bool GetRegValue(const char * key, const char * subkey, char * value)
{
    HKEY hKey;
    long error;
    DWORD size = 256;
    
    error = RegOpenKeyEx(HKEY_LOCAL_MACHINE, key, 0,
                         KEY_READ, &hKey);
    
    if (error == ERROR_SUCCESS) {
        error = RegQueryValueEx(hKey, subkey, 0, NULL, (BYTE*)value, &size);
    }
    
    return (error == ERROR_SUCCESS);
}



//-----------------------------------------------------------
int WINAPI
WinMain(HINSTANCE hInstance,
        HINSTANCE hPrevInstance,
        LPSTR cmdline,
        int nCmdShow)
{
    
    HKEY key;
    char jre_version  [MAX_PATH];
    char jre_subkey   [MAX_PATH];
    char jre_home     [MAX_PATH];
    char base_dir     [MAX_PATH];
    char lib_dir      [MAX_PATH];
    char classpath    [MAX_PATH];
    char cmd          [MAX_PATH];
    char temp         [MAX_PATH];

    
    //-----------------------------------------------
    // Load base directory
    //-----------------------------------------------
    GetModuleFileName(NULL, base_dir, MAX_PATH);
    *(strrchr(base_dir, '\\')) = '\0';

    
    //-----------------------------------------------
    // Expand the DLL search path
    //-----------------------------------------------
    sprintf(lib_dir, "lib");
    GetEnvironmentVariable("PATH", cmd, MAX_PATH);
    sprintf(temp, "%s;%s", cmd, lib_dir);
    SetEnvironmentVariable("PATH", temp);
    
    
    //-----------------------------------------------
    // Get the base jar file
    //-----------------------------------------------
    sprintf(classpath, "lib\\tern.jar");

    
    //-----------------------------------------------
    // Get the JRE directory
    //-----------------------------------------------
    if (!GetRegValue(JRE_REG, "CurrentVersion", jre_version)) {
        MessageBox(NULL, "No Java Runtime Available (1)", "Error!",
                   MB_ICONEXCLAMATION | MB_OK);
        return 0;
    }

    sprintf(jre_subkey, "%s\\%s", JRE_REG, jre_version);

    if (!GetRegValue(jre_subkey, "JavaHome", jre_home)) {
        MessageBox(NULL, "No Java Runtime Available (2)", "Error!",
                   MB_ICONEXCLAMATION | MB_OK);
        return 0;
    }        

    
    //-----------------------------------------------
    // Java Launch Command
    //-----------------------------------------------
    sprintf(cmd, "%s\\bin\\javaw.exe -Xmx512M -cp \"%s\" %s %s",
            jre_home,
            classpath,
            "tern.Main",
            cmdline);

    WinExec(cmd, SW_SHOW);

    return 0;
}
