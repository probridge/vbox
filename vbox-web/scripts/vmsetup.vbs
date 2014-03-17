Dim workDir, logFileName, strUserName, DiskPartExtendScript
Dim fso, strShareName, strSharePath, strShareDesc
workDir = "C:\vBoxUtil\"
logFileName = workDir & "startup_log.log"
strSharePath = "D:\"
strShareName = "MyFiles"
strShareDesc = "Personal Data Access"
strUserName = "ACEUser" 
DiskPartExtendScript = "diskpart.script"
Main
WScript.Quit(0)

'END OF EXECUTION - v3.1.1 build 20140317

Function Main
    On Error Resume Next
    Dim objNetwork, tmp, strPassword, cmd, strShortVMName, extendResult, initResult, shareResult
    Dim strCurrentUserName, strCurrentDomainName, strFullUserName
    Dim strComputerName, copyResult
    Dim WshShell, strHostName, strVMName, objRegEx
	Dim logger, count

    Set WshShell = WScript.CreateObject("WScript.Shell")
    Set objNetwork = WScript.CreateObject("WScript.Network")
    Set fso = WScript.CreateObject("Scripting.FileSystemObject") 

    set logger = fso.opentextfile(logFileName,8,true)
    logger.write "["+cstr(now())+"] Processing Start." & VbCrLf

    logger.write "Deleting previous password" & VbCrLf
    WshShell.RegDelete "HKLM\SOFTWARE\Microsoft\Virtual Machine\Guest\vBoxGuestOSPassword"

	count = 0
	Do While count < 10
		WScript.Sleep 1000
		count = count + 1
		strVMName = WshShell.RegRead("HKLM\SOFTWARE\Microsoft\Virtual Machine\Guest\Parameters\VirtualMachineName")
		logger.write "Waiting for integration service..." & count & "..." & VbCrLf
		If strVMName <> "" Then Exit Do
	Loop
    logger.write "VM Name: " & strVMName & VbCrLf
	
    strCurrentUserName = objNetwork.UserName
    strCurrentDomainName = objNetwork.UserDomain
    strComputerName = objNetwork.ComputerName
    
    strFullUserName = strCurrentDomainName & "\\" & strCurrentUserName
    logger.write "Computer Name: " & strComputerName & VbCrLf
    logger.write "Running under: " & strFullUserName & VbCrLf
    

    strShortVMName = Mid(strVMName,6,15)

    Set objRegEx = CreateObject("VBScript.RegExp")
    objRegEx.Global = True   
    objRegEx.Pattern = "[^a-zA-Z0-9]"
    strShortVMName = objRegEx.Replace(strShortVMName, "-")
    logger.write "Short VM Name: " & strShortVMName & VbCrLf

    If StrComp(strComputerName,strShortVMName,vbTextCompare)<>0 Then
		changeHostName strShortVMName
        logger.write "Setting Computer Name to " & strShortVMName & ", and reboot." & VbCrLf
        logger.close
		rebootSystem
		WScript.Quit(255)
    End If
    logger.write "Hostname OK, no Reboot Required" & VbCrLf

    strPassword = RandomString()
    WshShell.RegWrite "HKLM\SOFTWARE\Microsoft\Virtual Machine\Guest\vBoxGuestOSPassword",strPassword
    logger.write "Generated New Password: " & strPassword & VbCrLf

    changePwd strComputerName, strUserName, strPassword
    logger.write "Changed the password for " & strUserName & VbCrLf

    extendResult = tryExtendDisk
    logger.write "Extend Disk status code: " & extendResult & VbCrLf

    shareResult = setupShare(strSharePath, strShareName, strShareDesc, strUserName)
    logger.write "Create share status code: " & shareResult & VbCrLf
    
    logger.write "["+cstr(now())+"] Processing complete." & VbCrLf
    logger.close
End Function

Function rebootSystem()
	Dim OpSysSet,OpSys
	Set OpSysSet = GetObject("winmgmts:{authenticationlevel=Pkt,(Shutdown)}").ExecQuery( _
		"select * from Win32_OperatingSystem where Primary=true")
	for each OpSys in OpSysSet
		retVal = OpSys.Win32Shutdown(6)
	next
End Function

Function changeHostName(strNewName)
    Dim objWMIService,objComputer
    Set objWMIService = GetObject("Winmgmts:root\cimv2")
    For Each objComputer in _
        objWMIService.InstancesOf("Win32_ComputerSystem")
        objComputer.rename strNewName, NULL, NULL 
    Next
End Function

Function setupShare(strFolderName, strShareName, strShareDesc, strUser)
	Set Services = GetObject("winmgmts:{impersonationLevel=impersonate,(Security)}!\\.\root\cimv2") 
	' Connects to the WMI service with security privileges 
	Set SecDescClass = Services.Get("Win32_SecurityDescriptor") 
	' Need an instance of the Win32_SecurityDescriptor so we can create an instance of a Security Descriptor. 
	Set SecDesc = SecDescClass.SpawnInstance_() 
	' Create an instance of a Security Descriptor. 
	Set colWinAcc = Services.ExecQuery("SELECT * FROM Win32_ACCOUNT WHERE Name='" & strUser & "'") 
	If colWinAcc.Count < 1 Then 
	    wscript.quit 
	End If 
	' Find the WMI representation of a particular Windows Account 
	For Each refItem in colWinAcc 
	    Set refSID = Services.Get("Win32_SID='" & refItem.SID & "'") 
	    ' Get the SID for the choosen Windows account. 
	Next 
	Set refTrustee = Services.Get("Win32_Trustee").spawnInstance_() 
	' Creates an instance of a Windows Security Trustee (usually a user but anything with a SID I guess...) 
	With refTrustee 
	    .Domain = refSID.ReferencedDomainName 
	    .Name = refSID.AccountName 
	    .SID = refSID.BinaryRepresentation 
	    .SidLength = refSID.SidLength 
	    .SIDString = refSID.SID 
	End With 
	' Sets the trustee object up with the SID & all that malarkey from the user object we have choosen to work on 
	Set ACE = Services.Get("Win32_Ace").SpawnInstance_ 
	' Creates an instance of an Access Control Entry Object(this will be one entry on the access list on an object) 
	ACE.Properties_.Item("AccessMask") = 2032127 
	' This is full Control     ' This is full Control (bitflag) full list here: http://msdn.microsoft.com/library/default.asp?url=/library/en-us/wmisdk/wmi/win32_ace.asp 
	ACE.Properties_.Item("AceFlags") = 3 
	' what to apply ACE to inc inhehitance 3 - means files & folders get permssions & pass onto children 
	ACE.Properties_.Item("AceType") = 0 
	' 0=allow access 1=deny access 
	ACE.Properties_.Item("Trustee") = refTrustee 
	' Set the Trustee (user) that this Access control Entry will refer to. 
	SecDesc.Properties_.Item("DACL") = Array(ACE) 
	' Get the DACL property of the Security Descriptor object 
	' Add the ACE to the Dynamic Access Control List on the object (an array) it will overwrite the old entries  
	' unless you retreive & save 'em first & add them to a big array with the new entry as well as the old ones 
	Set Share = Services.Get("Win32_Share") 
	' Get a WMI share Object 
	Set InParam = Share.Methods_("Create").InParameters.SpawnInstance_() 
	' Create an instance of a WMI input Parameters object 
	InParam.Properties_.Item("Access") = SecDesc 
	' Set the Access Parameter to the Security Descriptor Object we configured above 
	InParam.Properties_.Item("Description") = strShareDesc 
	InParam.Properties_.Item("Name") = strShareName 
	InParam.Properties_.Item("Path") = strFolderName 
	InParam.Properties_.Item("Type") = 0 
	Set outParams=Share.ExecMethod_("Create", InParam)
	' Create the share with all the parameters we have set up 
End Function

Function tryExtendDisk()
	Dim WshShell, oExec, input
	Set WshShell = CreateObject("WScript.Shell")
	Set oExec    = WshShell.Exec("diskpart /s " & workDir  & DiskPartExtendScript)
	Do While oExec.Status = 0
		 WScript.Sleep 100
	Loop
	tryExtendDisk = oExec.ExitCode
End Function

Function changePwd(strComputerName, strFullUserName, strPassword)
	Dim objUser
	Set objUser = GetObject("WinNT://" & strComputerName & "/" + strFullUserName)
	objUser.SetPassword(strPassword)
End Function

Function RandomString()
    Randomize()
    dim CharacterSetArray
    CharacterSetArray = Array(_
        Array(5, "abcdefghijklmnopqrstuvwxyz"), _
        Array(1, "ABCDEFGHIJKLMNOPQRSTUVWXYZ"), _
        Array(1, "0123456789"), _
        Array(1, "!@#$+-*&?:") _
    )

    dim i, j, Count, Chars, Index, Temp
    for i = 0 to UBound(CharacterSetArray)
        Count = CharacterSetArray(i)(0)
        Chars = CharacterSetArray(i)(1)
        for j = 1 to Count
            Index = Int(Rnd() * Len(Chars)) + 1
            Temp = Temp & Mid(Chars, Index, 1)
        next
    next

    dim TempCopy
    do until Len(Temp) = 0
        Index = Int(Rnd() * Len(Temp)) + 1
        TempCopy = TempCopy & Mid(Temp, Index, 1)
        Temp = Mid(Temp, 1, Index - 1) & Mid(Temp, Index + 1)
    loop
    RandomString = TempCopy
end function
