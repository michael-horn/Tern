//------------------------------------------------------------------------------
// File: webcam.cpp
//------------------------------------------------------------------------------
//#include <strsafe.h>
#include <windows.h>
#include <stdio.h>
#include <qedit.h>
#include "webcam.h"
#include "graph.h"

class SampleGrabberCallback;

static IGraphBuilder  * graph = NULL;
static IMediaControl  * media_control = NULL;
static IMediaEventEx  * media_event = NULL;
static ISampleGrabber * grabber = NULL;
static IBaseFilter    * src_filter = NULL;
static IBaseFilter    * null_filter = NULL;
static IBaseFilter    * grabber_filter = NULL;
static ICaptureGraphBuilder2 * builder = NULL;
static SampleGrabberCallback * sg_callback;

static boolean initialized = FALSE;
static boolean open = FALSE;
static int wcount = 0;


//==============================================================================
// ISampleGrabberCB Callback Class
//==============================================================================
class SampleGrabberCallback : public ISampleGrabberCB
{

private:
    boolean capture;
    boolean ready;
    jbyte * buffer;
    long fb_size;

public:

    SampleGrabberCallback() {
        this->capture = false;
        this->ready   = false;
        this->buffer  = NULL;
        this->fb_size = 0;
    }


    ~SampleGrabberCallback() {
		if (buffer != NULL) delete buffer;
	}

    
//--------------------------------------------------------------------
// To capture a frame, set the capture flag to true and wait for the
// next callback 
//--------------------------------------------------------------------
    void captureFrame() {
        this->ready = false;
        this->capture = true;
    }


    boolean isFrameReady() {
        return this->ready;
    }


    jbyte * getFrameBuffer() {
        return this->buffer;
    }


    long getFrameBufferSize() {
        return this->fb_size;
    }

    
    STDMETHODIMP SampleCB(double time, IMediaSample * sample) {
        HRESULT hr = S_OK;
        long size = 0;
        jbyte * p = NULL;

        if (capture) {
            this->capture = FALSE;
            size = sample->GetSize();
			if (size > 0) {

                     // get a pointer to the frame buffer
			    hr = sample->GetPointer((BYTE**)(&p));
				if (hr == S_OK && p != NULL) {

					// allocate our own buffer if necessary 
					if (buffer == NULL || size != fb_size) {
						if (buffer != NULL) delete buffer;
						this->buffer = new jbyte[size];
						this->fb_size = size;
					}

					// copy frame buffer --
					// unfortunately we have to do this here or we're in danger of 
					// an access violation: TODO, could do memory processing here 
					// rather than in WebCam.java...
					memcpy(buffer, p, size);
		            this->ready = TRUE;
				}
			}
        }
        return hr;
	}


    
//----------------------------------------------------------------------------
// COM IUnknown interface    
//----------------------------------------------------------------------------
    // Fake referance counting.
    STDMETHODIMP_(ULONG) AddRef() { return 1; }
    STDMETHODIMP_(ULONG) Release() { return 2; }
    
    STDMETHODIMP QueryInterface(REFIID riid, void **ppvObject) {
        if (NULL == ppvObject) return E_POINTER;
        if (riid == __uuidof(IUnknown)) {
            *ppvObject = static_cast<IUnknown*>(this);
            return S_OK;
        }
        if (riid == __uuidof(ISampleGrabberCB)) {
            *ppvObject = static_cast<ISampleGrabberCB*>(this);
            return S_OK;
        }
        return E_NOTIMPL;
    }

    STDMETHODIMP BufferCB(double Time, BYTE *pBuffer, long BufferLen) {
        return E_NOTIMPL;
    }


};



//--------------------------------------------------------------------
JNIEXPORT void JNICALL
Java_webcam_WebCam_initialize(JNIEnv * env, jobject obj) {
    
    HRESULT hr;

    // Initialize COM
    if (FAILED(CoInitialize(NULL))) {
        throwWebCamException(env, "CoInitialize failed.");
        return;
    }

        
    // Create graph builder
    hr = CoCreateInstance (CLSID_FilterGraph, NULL, CLSCTX_INPROC,
                           IID_IGraphBuilder, (void **) &graph);
    if (FAILED(hr)) {
        throwWebCamException(env, "Create IGraphBuilder failed.");
        return;
    }


    // Create CaptureGraphBuilder2
    hr = CoCreateInstance (CLSID_CaptureGraphBuilder2 , NULL, CLSCTX_INPROC,
                           IID_ICaptureGraphBuilder2, (void **) &builder);
    if (FAILED(hr)) {
        throwWebCamException(env, "Create ICaptureGraphBuilder2 failed.");
        return;
    }

    
    // Create NullRenderer
    hr = CoCreateInstance(CLSID_NullRenderer, NULL, CLSCTX_INPROC_SERVER, 
                          IID_IBaseFilter, (void**)&null_filter);
    if (FAILED(hr)) {
        throwWebCamException(env, "Create NullRenderer failed.");
        return;
    }

        
    // Create sample grabber
    hr = CoCreateInstance(CLSID_SampleGrabber, NULL, CLSCTX_INPROC_SERVER, 
                          IID_IBaseFilter, (void**)&grabber_filter);
    if (FAILED(hr)) {
        throwWebCamException(env, "Create SampleGrabber failed.");
        return;
    }

        
    // Create media controller
    hr = graph->QueryInterface(IID_IMediaControl,(LPVOID *) &media_control);
    if (FAILED(hr)) {
        throwWebCamException(env, "Create IMediaControl failed.");
        return;
    }

        
    // Create media event
    hr = graph->QueryInterface(IID_IMediaEvent, (LPVOID *) &media_event);
    if (FAILED(hr)) {
        throwWebCamException(env, "Create IMediaEvent failed.");
        return;
    }

        
    // Set filtergraph
    hr = builder->SetFiltergraph(graph);
    if (FAILED(hr)) {
        throwWebCamException(env, "Failed to install filter graph.");
        return;
    }

        
    // Add null renderer to the graph
    hr = graph->AddFilter(null_filter, L"NullRender");
    if (FAILED(hr)) {
        throwWebCamException(env, "Failed to add null filter to graph.");
        return;
    }

        
    // Add sample grabber to the graph
    hr = graph->AddFilter(grabber_filter, L"SampleGrab");
    if (FAILED(hr)) {
        throwWebCamException(env, "Failed to add sample grabber to graph.");
        return;
    }


    // Get ISampleGrabber interface
    hr = grabber_filter->QueryInterface(IID_ISampleGrabber, (void**)&grabber);
    if (FAILED(hr)) {
        throwWebCamException(env, "Failed to query ISampleGrabber.");
        return;
    }


    // create the callback class
    sg_callback = new SampleGrabberCallback();

    // Set media type of frame grabber
    AM_MEDIA_TYPE mt;
    ZeroMemory(&mt, sizeof(AM_MEDIA_TYPE));
    mt.majortype = MEDIATYPE_Video;
    mt.subtype = MEDIASUBTYPE_RGB24;
    grabber->SetMediaType(&mt);
    grabber->SetOneShot(FALSE);
    grabber->SetBufferSamples(FALSE); 
    grabber->SetCallback(sg_callback, 0);

    
    initialized = TRUE;
}



//--------------------------------------------------------------------
JNIEXPORT void JNICALL
Java_webcam_WebCam_NopenCamera(JNIEnv * env, jobject obj,
                               jint width, jint height)
{

    if (!initialized) return;
    
    
    // Connect to camera
    HRESULT hr = FindCaptureDevice(&src_filter);
    if (FAILED(hr)) {
        throwWebCamException(env,
                             "Failed to connect to webcam. " 
                             "Make sure the device is plugged in.");
        return;
    }

        
    // Add source filter to the graph
    hr = graph->AddFilter(src_filter, L"Video Capture");
    if (FAILED(hr)) {
        throwWebCamException(env, "Failed to add source filter to graph.");
        return;
    }


    // Connect sample grabber
    hr = ConnectFilters(graph, src_filter, grabber_filter);
    if (FAILED(hr)) {
        throwWebCamException(env, "Failed to connect sample grabber.");
        return;
    }

    
    // Connect null renderer
    hr = ConnectFilters(graph, grabber_filter, null_filter);
    if (FAILED(hr)) {
        throwWebCamException(env, "Failed to connect null filter.");
        return;
    }

    hr = setImageDimensions ((int)width, (int)height);
    if (FAILED(hr)) {
        throwWebCamException(env, "Unsupported image dimensions");
        return;
    }

    
    hr = media_control->Run();
    if (FAILED(hr)) {
        throwWebCamException(env, "Failed to run graph.");
        return;
    }


        // initialize frame buffer
    open = TRUE;
}



//--------------------------------------------------------------------
HRESULT setImageDimensions (int width, int height) {
    
    HRESULT hr;
    int count, size;
    AM_MEDIA_TYPE * pmt = NULL;
    VIDEO_STREAM_CONFIG_CAPS scc;
    IAMStreamConfig * stream_config = NULL;

    hr = builder->FindInterface(NULL, NULL, src_filter,
                                IID_IAMStreamConfig, (void**)&stream_config);
    if (FAILED(hr)) return hr;

    stream_config->GetNumberOfCapabilities(&count, &size);
    for (int i=0; i<count; i++) {
        hr = stream_config->GetStreamCaps(i, &pmt, (byte*)(&scc));

        if (hr == S_OK &&
            scc.MaxOutputSize.cx == width &&
            scc.MaxOutputSize.cy == height)
        {
            hr = stream_config->SetFormat(pmt);
            if (hr == S_OK) {
                stream_config->Release();
                return S_OK;
            }
        }
    }
    if (stream_config) stream_config->Release();
    return E_FAIL;
}
        


//--------------------------------------------------------------------
JNIEXPORT void JNICALL
Java_webcam_WebCam_capture (JNIEnv * env, jobject obj, jstring filename)
{

    if (!initialized || !open) return;
    
    HANDLE file;
    HRESULT hr;
    AM_MEDIA_TYPE mt;
    VIDEOINFOHEADER *info;

    long size = 0;
    char * buffer = NULL;
    const char * fname = NULL;

    fname = env->GetStringUTFChars(filename, 0);

    try {

        // Determine the image buffer size
        hr = grabber->GetCurrentBuffer(&size, NULL);
        if (FAILED(hr)) {
            throwWebCamException(env, "Failed to capture image.");
            throw hr;
        }

        // Transfer the image to a buffer
        buffer = new char[size];
        hr = grabber->GetCurrentBuffer(&size, (long*)buffer);
        if (FAILED(hr)) {
            throwWebCamException(env, "Failed to capture image.");
            throw hr;
        }

        // Get media type of buffer
        hr = grabber->GetConnectedMediaType(&mt);
        if (FAILED(hr)) {
            throwWebCamException(env, "Unable to determine buffer type.");
            throw hr;
        }
        
        // Examine the format block.
        if ((mt.formattype == FORMAT_VideoInfo) && 
            (mt.cbFormat >= sizeof(VIDEOINFOHEADER)) &&
            (mt.pbFormat != NULL) ) 
        {
            info = (VIDEOINFOHEADER*)mt.pbFormat;
        } else {
            throwWebCamException(env, "Invalid media type.");
            throw VFW_E_INVALIDMEDIATYPE; 
        }

        // Create file
        file = CreateFile(fname,
                          GENERIC_WRITE,
                          FILE_SHARE_WRITE,
                          NULL,
                          CREATE_ALWAYS,
                          0, NULL);

        // Setup bitmap header
        long cbBitmapInfoSize = mt.cbFormat - SIZE_PREHEADER;
        BITMAPFILEHEADER bfh;
        ZeroMemory(&bfh, sizeof(bfh));
        bfh.bfType = 'MB';  // Little-endian for "MB".
        bfh.bfSize = sizeof( bfh ) + size + cbBitmapInfoSize;
        bfh.bfOffBits = sizeof( BITMAPFILEHEADER ) + cbBitmapInfoSize;
        
        // Write the file header.
        DWORD dwWritten = 0;
        WriteFile( file, &bfh, sizeof( bfh ), &dwWritten, NULL );
        WriteFile( file, HEADER(info), cbBitmapInfoSize, &dwWritten, NULL );

        // Write image data
        WriteFile( file, buffer, size, &dwWritten, NULL );
        
        CloseHandle( file );
        FreeMediaType( &mt );
    }
    catch (HRESULT) { }
        
    if (buffer != NULL) delete buffer;
}


//--------------------------------------------------------------------
JNIEXPORT void JNICALL
Java_webcam_WebCam_captureFrame (JNIEnv * env, jobject obj)
{
	int count = 0;
    if (!initialized || !open) return;

    sg_callback->captureFrame();
	while (!sg_callback->isFrameReady()) { Sleep(1); }

	// Send data back to java via the callback
    jclass c = env->GetObjectClass(obj);
    jmethodID method = env->GetMethodID(c, "callback", "([B)V");
    if (method != NULL) {
        long size = sg_callback->getFrameBufferSize();
        jbyte * buff = sg_callback->getFrameBuffer();
        jbyteArray arr = env->NewByteArray(size);
        env->SetByteArrayRegion(arr, 0, size, buff);
        env->CallVoidMethod(obj, method, arr);
    }
}
    


//--------------------------------------------------------------------
JNIEXPORT void JNICALL
Java_webcam_WebCam_closeCamera (JNIEnv * env, jobject obj)
{
    open = FALSE;

    
    // Stop previewing data
    if (media_control) media_control->StopWhenReady();

    // Release src filter
    if (src_filter) src_filter->Release();

    src_filter = NULL;

}



//--------------------------------------------------------------------
JNIEXPORT void JNICALL
Java_webcam_WebCam_uninitialize (JNIEnv * env, jobject obj)
{
    initialized = FALSE;
    
    // Release DirectShow interfaces
    if (graph)           graph->Release();
    if (builder)         builder->Release();
    if (grabber)         grabber->Release();
    if (null_filter)     null_filter->Release();
    if (media_event)     media_event->Release();
    if (media_control)   media_control->Release();
    if (grabber_filter)  grabber_filter->Release();
    if (sg_callback)     delete sg_callback;

    graph          = NULL;
    builder        = NULL;
    grabber        = NULL;
    null_filter    = NULL;
    grabber_filter = NULL;
    media_event    = NULL;
    media_control  = NULL;
    sg_callback    = NULL;
    
    CoUninitialize();
}



//--------------------------------------------------------------------
JNIEXPORT jboolean JNICALL
Java_webcam_WebCam_isCameraOpen (JNIEnv * env, jobject obj) {
    return (open && initialized);
}



//-------------------------------------------------------------------
void throwWebCamException(JNIEnv * env, const char * message) {
    jclass x = env->FindClass("webcam/WebCamException");
    if (x) env->ThrowNew(x, message);
}



