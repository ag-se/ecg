#include <windows.h>
#include <string.h>
#include "de_fu_berlin_inf_atl_ntps_NTProcess.h"

#define MaxProcessNumber 1024


typedef BOOL (WINAPI *ENUMPROCESSES)(
	DWORD * lpidProcess,  
  	DWORD cb,             
  	DWORD * cbNeeded      
);

typedef BOOL (WINAPI *ENUMPROCESSMODULES)(
	HANDLE hProcess,      
	HMODULE * lphModule,  
	DWORD cb,             
	LPDWORD lpcbNeeded    
);

typedef DWORD (WINAPI *GETMODULEFILENAMEEXA)( 
	HANDLE hProcess,		
	HMODULE hModule,		
	LPTSTR lpstrFileName,	
	DWORD nSize			
);

typedef DWORD (WINAPI *GETMODULEBASENAME)( 
	HANDLE hProcess,		
	HMODULE hModule,		
	LPTSTR lpstrFileName,	
	DWORD nSize			
);

typedef struct _PROCESS_MEMORY_COUNTERS {
    DWORD cb;
    DWORD PageFaultCount;
    DWORD PeakWorkingSetSize;
    DWORD WorkingSetSize;
    DWORD QuotaPeakPagedPoolUsage;
    DWORD QuotaPagedPoolUsage;
    DWORD QuotaPeakNonPagedPoolUsage;
    DWORD QuotaNonPagedPoolUsage;
    DWORD PagefileUsage;
    DWORD PeakPagefileUsage;
} PROCESS_MEMORY_COUNTERS;

typedef PROCESS_MEMORY_COUNTERS *PPROCESS_MEMORY_COUNTERS;


typedef BOOL (WINAPI *GETPROCESSMEMORYINFO)(
	HANDLE hProcess,
	PPROCESS_MEMORY_COUNTERS ppsmenCounters,
	DWORD cb
	
);

ENUMPROCESSES EnumProcesses;
ENUMPROCESSMODULES EnumProcessModules;
GETMODULEFILENAMEEXA GetModuleFileNameExA;
GETMODULEBASENAME GetModuleBaseName;
GETPROCESSMEMORYINFO GetProcessMemoryInfo;



BOOL APIENTRY DllMain(HANDLE hInst, DWORD ul_reason_being_called, LPVOID lpReserved){     
	return TRUE; 
}; 

/*
 * Class:     NTProcess
 * Method:    Initialize
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_de_fu_1berlin_inf_atl_ntps_NTProcess_Initialize(JNIEnv * env,jclass clazz){
	
	HANDLE hpsapi=LoadLibrary("PSAPI.DLL");
	
	if (hpsapi==NULL) return FALSE;
    
	EnumProcesses=(ENUMPROCESSES)GetProcAddress((HINSTANCE)hpsapi,"EnumProcesses");
	
	GetModuleFileNameExA = (GETMODULEFILENAMEEXA)GetProcAddress((HINSTANCE)hpsapi, "GetModuleFileNameExA");

	GetModuleBaseName = (GETMODULEBASENAME)GetProcAddress((HINSTANCE)hpsapi, "GetModuleBaseNameA");

	EnumProcessModules = (ENUMPROCESSMODULES)GetProcAddress((HINSTANCE)hpsapi, "EnumProcessModules");
	
	GetProcessMemoryInfo = (GETPROCESSMEMORYINFO)GetProcAddress((HINSTANCE)hpsapi, "GetProcessMemoryInfo");

	if (
		
		NULL == EnumProcesses		|| 
		NULL == GetModuleFileName	|| 
		NULL == GetModuleBaseName	||
		NULL == EnumProcessModules  )
        return FALSE;

	return TRUE;    

};

/*
 * Class:     NTProcess
 * Method:    EnumProcesses
 * Signature: ()[I
 */
JNIEXPORT jintArray JNICALL Java_de_fu_1berlin_inf_atl_ntps_NTProcess_EnumProcesses(JNIEnv * env, jclass clazz){
	DWORD aPids[MaxProcessNumber];
	DWORD cGot;
	jintArray Pids=0;

	if(EnumProcesses(aPids,sizeof(aPids),&cGot)){
		cGot /= sizeof(aPids[0]);
        	Pids= (*env)->NewIntArray(env,cGot);
		(*env)->SetIntArrayRegion(env,Pids,0,cGot,(jint*) aPids);
	};

	return Pids;
};

/*
 * Class:     NTProcess
 * Method:    OpenProcess
 * Signature: (IZI)I
 */
JNIEXPORT jint JNICALL Java_de_fu_1berlin_inf_atl_ntps_NTProcess_OpenProcess (JNIEnv * env, jclass clazz,jint Pid){

	return (jint) OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ,FALSE,Pid);

};

/*
 * Class:     NTProcess
 * Method:    EnumProcessModules
 * Signature: (I[I)[I
 */
JNIEXPORT jintArray JNICALL Java_de_fu_1berlin_inf_atl_ntps_NTProcess_EnumProcessModules (JNIEnv * env, jclass clazz, jint hProcess){
	HMODULE hModule[MaxProcessNumber];
	jintArray jModule=0;
	DWORD cGot;     
        
	if (EnumProcessModules((HANDLE)hProcess,hModule,sizeof(hModule),&cGot)){
		cGot/= sizeof(hModule[0]);
		jModule= (*env)->NewIntArray(env,cGot);
		(*env)->SetIntArrayRegion(env,jModule,0,cGot,(jint*)hModule);
	};
    
	return jModule;
     
};

/*
 * Class:     NTProcess
 * Method:    GetModuleFileName
 * Signature: (II)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_de_fu_1berlin_inf_atl_ntps_NTProcess_GetModuleFileName(JNIEnv * env, jclass clazz, jint hProcess, jint hModule){
	jstring jName=0;
	char FileName[MAX_PATH];

	if(GetModuleFileNameExA((HANDLE)hProcess,(HMODULE)hModule,FileName,sizeof(FileName))!=0){
		jName=(*env)->NewStringUTF(env,FileName);
    };

	return jName;
        	
};

/*
 * Class:     NTProcess
 * Method:    GetModuleBaseName
 * Signature: (II)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_de_fu_1berlin_inf_atl_ntps_NTProcess_GetModuleBaseName(JNIEnv * env, jclass clazz, jint hProcess, jint hModule){
	jstring jName=0;
	char FileName[MAX_PATH];

	if(GetModuleBaseName((HANDLE)hProcess,(HMODULE)hModule,FileName,sizeof(FileName))!=0){
		jName=(*env)->NewStringUTF(env,FileName);
    };

	return jName;
};


/*
 * Class:     NTProcess
 * Method:    CloseHandle
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_de_fu_1berlin_inf_atl_ntps_NTProcess_CloseHandle(JNIEnv * env, jclass clazz, jint handle)
{
	return CloseHandle((HANDLE) handle);
};

/*
 * Class:     NTProcess
 * Method:    GetProcessMemoryInfo
 * Signature: (I)LMemoryInfo;
 */
JNIEXPORT jobject JNICALL Java_de_fu_1berlin_inf_atl_ntps_NTProcess_GetProcessMemoryInfo(JNIEnv * env, jclass clazz, jint hProcess){
	jfieldID jfield;
	jobject jobj=0;
	PROCESS_MEMORY_COUNTERS pmc;    
    
    	if(hProcess ==0) return 0;

    	if ( GetProcessMemoryInfo((HANDLE) hProcess, &pmc, sizeof(pmc)) ){
		clazz=(*env)->FindClass(env,"MemoryInfo");
		if (clazz==0) return 0;
		jobj = (*env)->AllocObject (env,clazz);
		//set MemoryInfo object field
		jfield=(*env)->GetFieldID(env,clazz,"cb","I");
		(*env)->SetIntField (env,jobj, jfield,pmc.cb);
        jfield=(*env)->GetFieldID(env,clazz,"PageFaultCount","I");
		(*env)->SetIntField (env,jobj, jfield,pmc.PageFaultCount);	
	    jfield=(*env)->GetFieldID(env,clazz,"PeakWorkingSetSize","I");
		(*env)->SetIntField (env,jobj, jfield,pmc.PeakWorkingSetSize);
		jfield=(*env)->GetFieldID(env,clazz,"WorkingSetSize","I");
		(*env)->SetIntField (env,jobj, jfield,pmc.WorkingSetSize);
		jfield=(*env)->GetFieldID(env,clazz,"QuotaPeakPagedPoolUsage","I");
		(*env)->SetIntField (env,jobj, jfield,pmc.QuotaPeakPagedPoolUsage);
		jfield=(*env)->GetFieldID(env,clazz,"QuotaPagedPoolUsage","I");
		(*env)->SetIntField (env,jobj, jfield,pmc.QuotaPagedPoolUsage);
		jfield=(*env)->GetFieldID(env,clazz,"QuotaPeakNonPagedPoolUsage","I");
		(*env)->SetIntField (env,jobj, jfield,pmc.QuotaPeakNonPagedPoolUsage);
		jfield=(*env)->GetFieldID(env,clazz,"QuotaNonPagedPoolUsage","I");
		(*env)->SetIntField (env,jobj, jfield,pmc.QuotaNonPagedPoolUsage);
		jfield=(*env)->GetFieldID(env,clazz,"PagefileUsage","I");
		(*env)->SetIntField (env,jobj, jfield,pmc.PagefileUsage);
		jfield=(*env)->GetFieldID(env,clazz,"PeakPagefileUsage","I");
		(*env)->SetIntField (env,jobj, jfield,pmc.PeakPagefileUsage);
	};

	return jobj;
};
