Set WshShell = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")
scriptDir = fso.GetParentFolderName(WScript.ScriptFullName)

batPath = scriptDir & "\launch_game.bat"
If Not fso.FileExists(batPath) Then
    batPath = fso.GetParentFolderName(scriptDir) & "\launch_game.bat"
End If
If Not fso.FileExists(batPath) Then
    batPath = "C:\Users\Никита\Downloads\HorusClient\launch_game.bat"
End If

workDir = fso.GetParentFolderName(batPath)
WshShell.CurrentDirectory = workDir

cmdLine = "cmd.exe /c call """ & batPath & """ /silent"
WshShell.Run cmdLine, 0, True
