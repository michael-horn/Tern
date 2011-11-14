//====================================================================
// graph.h
//====================================================================

#ifndef _GRAPH_H_
#define _GRAPH_H_

//#include <atlbase.h>
#define NO_DSHOW_STRSAFE
#include <dshow.h>
#include "CComPtr.h"


HRESULT GetUnconnectedPin(
    IBaseFilter *pFilter,   // Pointer to the filter.
    PIN_DIRECTION PinDir,   // Direction of the pin to find.
    IPin **ppPin);          // Receives a pointer to the pin.


HRESULT ConnectFilters(
    IGraphBuilder *pGraph, // Filter Graph Manager.
    IPin *pOut,            // Output pin on the upstream filter.
    IBaseFilter *pDest);   // Downstream filter.


HRESULT ConnectFilters(
    IGraphBuilder *pGraph, 
    IBaseFilter *pSrc, 
    IBaseFilter *pDest);


HRESULT FindCaptureDevice(IBaseFilter ** ppSrcFilter);


void FreeMediaType(AM_MEDIA_TYPE * pmt);

#endif
