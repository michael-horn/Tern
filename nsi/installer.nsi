;-----------------------------------------------------------------
; NSIS Installer Script
;-----------------------------------------------------------------
!include "MUI2.nsh"

!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\orange-install.ico"
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\orange-uninstall.ico"
 
; MUI Settings / Header
!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_RIGHT
!define MUI_HEADERIMAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Header\orange-r.bmp"
!define MUI_HEADERIMAGE_UNBITMAP "${NSISDIR}\Contrib\Graphics\Header\orange-uninstall-r.bmp"
 
; MUI Settings / Wizard
!define MUI_WELCOMEFINISHPAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Wizard\orange.bmp"
!define MUI_UNWELCOMEFINISHPAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Wizard\orange-uninstall.bmp"


!define JRE_VERSION "1.6"
!define JRE_URL "http://javadl.sun.com/webapps/download/AutoDL?BundleId=24936"
!define JAVAEXE "javaw.exe"

!define LOGITECH_URL "http://www.logitech.com/index.cfm/435/3056"

Name "Tern Tangible Programming"
OutFile "Tern-Setup.exe"
InstallDir $PROGRAMFILES\Tern
InstallDirRegKey HKLM "Software\Tern" "Install_Dir"
RequestExecutionLevel user
;LicenseData "..\LICENSE"

;--------------------------------
; Pages
;--------------------------------
!define MUI_WELCOMEPAGE_TEXT "This wizard will guide you through the installation of the Tern Tangible Programming System.  Click Next to continue."
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "..\LICENSE"
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!define MUI_FINISHPAGE_SHOWREADME ${LOGITECH_URL}
!define MUI_FINISHPAGE_SHOWREADME_NOTCHECKED
!define MUI_FINISHPAGE_SHOWREADME_TEXT "Download Logitech QuickCam Pro 9000 drivers..."
!insertmacro MUI_PAGE_FINISH
 
!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

!insertmacro MUI_LANGUAGE "English"


;-----------------------------------------------------------------
Function GetJRE
   Push $R0
   Push $R1
   Push $2

   ;----------------------------------------------------
   ; Check the current JRE version
   ;----------------------------------------------------
   ClearErrors
   ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
   ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
   StrCmp $R1 ${JRE_VERSION} 0 DownloadJRE
   StrCpy $R0 "$R0\bin\${JAVAEXE}"
   MessageBox MB_ICONINFORMATION "The Java 6 Runtime Environment is already installed."
   IfFileExists $R0 JREFound DownloadJRE
 
   ;----------------------------------------------------
   ; Download JRE Installer
   ;----------------------------------------------------
   DownloadJRE:
     ; Call ElevateToAdmin
     MessageBox MB_ICONINFORMATION "Downloading Java Runtime Environment ${JRE_VERSION}."
     StrCpy $2 "$TEMP\Java Runtime Environment.exe"
     nsisdl::download /TIMEOUT=30000 ${JRE_URL} $2
     Pop $R0 ;Get the return value
     StrCmp $R0 "success" +3
     MessageBox MB_ICONSTOP "Download failed: $R0"
     Abort
     ExecWait $2
     Delete $2
 
     ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
     ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
     StrCmp $R1 ${JRE_VERSION} 0 GoodLuck
     StrCpy $R0 "$R0\bin\${JAVAEXE}"
     IfFileExists $R0 JREFound GoodLuck
 
  GoodLuck:
    StrCpy $R0 "${JAVAEXE}"
    MessageBox MB_ICONSTOP "Cannot find appropriate Java Runtime Environment."
    Abort
 
  JREFound:
    Pop $2
    Pop $R1
    Exch $R0

FunctionEnd


;-----------------------------------------------------------------
Section "Tern (required)"

  SectionIn RO
  SetOutPath $INSTDIR
  
  ; Put file there
  File ..\COPYRIGHT
  File ..\LICENSE
  File ..\README
  File ..\Tern.exe
  File ..\config.properties
  File /r /x .svn /x *.svg /x *.doc ..\docs
  File /r /x .svn /x *.exp /x *.lib ..\lib
  File /r /x .svn /x classes ..\nqc
  File /r /x .svn ..\RCXTowerDrivers

  ; Write the installation path into the registry
  WriteRegStr HKLM SOFTWARE\Tern "Install_Dir" "$INSTDIR"
  
  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Tern" "DisplayName" "Tern Tangible Programming"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Tern" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Tern" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Tern" "NoRepair" 1
  WriteUninstaller "uninstall.exe"
  
SectionEnd ; end the section


;-----------------------------------------------------------------
Section "Start Menu Shortcut"

  CreateDirectory "$SMPROGRAMS\Tern"
  CreateShortCut "$SMPROGRAMS\Tern\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0
  CreateShortCut "$SMPROGRAMS\Tern\Tern.lnk" "$INSTDIR\Tern.exe" "" "$INSTDIR\Tern.exe" 0
SectionEnd

;-----------------------------------------------------------------
Section "Desktop Shortcut"
  CreateShortCut "$DESKTOP\Tern.lnk" "$INSTDIR\Tern.exe" "" "$INSTDIR\Tern.exe" 0
SectionEnd

;-----------------------------------------------------------------
Section "Java 6 Runtime"
  Call GetJRE
SectionEnd


;-----------------------------------------------------------------
Section "LEGO USB Tower Driver"
  MessageBox MB_ICONINFORMATION "Installing the LEGO Mindstorms USB tower drivers..."
  ExecWait "$INSTDIR\RCXTowerDrivers\Setup.exe"
SectionEnd


;-----------------------------------------------------------------
Section "Uninstall"
  
  ; Remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Tern"
  DeleteRegKey HKLM SOFTWARE\Tern

  ; Remove files and uninstaller
  ; Delete $INSTDIR\installer.nsi
  ; Delete $INSTDIR\uninstall.exe

  ; Remove shortcuts, if any
  Delete "$SMPROGRAMS\Tern\*.*"
  Delete "$DESKTOP\Tern.lnk"

  ; Remove directories used
  RMDir "$SMPROGRAMS\Tern"
  RMDir /r "$INSTDIR"

SectionEnd
