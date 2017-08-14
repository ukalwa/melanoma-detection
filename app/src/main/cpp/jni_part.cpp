#include <jni.h>
#include <vector>

#include "linked_list.hpp"
#include "ac_withoutedges_yuv.hpp"

#include <opencv2/imgproc/imgproc.hpp>

using namespace std;
using namespace cv;

extern "C" {
	JNIEXPORT void JNICALL Java_ukalwa_moledetection_ProcessImage_ActiveContour(JNIEnv*, jobject,
                                                                                jlong addrRgb,
                                                                                jlong addrZero,
                                                                                jlong addrHsv);

	JNIEXPORT void JNICALL Java_ukalwa_moledetection_ProcessImage_ActiveContour(JNIEnv*, jobject,
                                                                                jlong addrRgb,
                                                                                jlong addrZero,
                                                                                jlong addrHsv)
	{
        // Initialize the Mat objects received from JNI Native call
		Mat& mRgb  = *(Mat*)addrRgb; // HSV image 
		Mat& mZero  = *(Mat*)addrZero; // Zero image to draw active contour border
		Mat& mHsv = *(Mat*)addrHsv;

        //Initialize params
        int iteration_params[] = {100, 50};
        double gaussian_params[] = {5,1.0};
        int energy_params[] = { 1, 1, 1, 1, 1};
        double init_width = 0.85;
        double init_height = 0.85;

        // Initialize the output contour pixel list
		const ofeli::List* Lout1;
		//		const ofeli::List* Lin1;

        // Get image height and width
		int img1_width = mRgb.cols;
		int img1_height = mRgb.rows;

        // Convert the Mat object to flattened unsigned char array so that we can send this array
        // along with the initialization params to the active contour method
		unsigned char* img1_rgb_data = (unsigned char*)mRgb.data;
		unsigned char* img1_gray_data = (unsigned char*)mZero.data;
		ofeli::ACwithoutEdgesYUV ac(img1_rgb_data, img1_width, img1_height, true, init_width,
                                    init_height, 0.0, 0.0, true, (int) gaussian_params[0],
                                    gaussian_params[1], true, iteration_params[0],
                                    iteration_params[1], energy_params[0], energy_params[1],
                                    energy_params[2], energy_params[3], energy_params[4]);
		//ac.evolve_to_final_state();
		int j; // offset of the current pixel
		// evolve 100 times the active contour
		ac.evolve_n_iterations(400);

		//	ac.evolve_to_final_state();
        // get the contour pixel list
		Lout1 = &ac.get_Lout();
		//	Lin1 = &ac.get_Lin();

		// set all the pixel values in contour list to 255 on the zero image passed
		for( ofeli::List::const_Link iterator = Lout1->get_begin(); !iterator->end(); 
             iterator = iterator->get_next() )
		{
			j=iterator->get_elem();
			// Get flattened array pixel value
			//		i = 3*j;
			//img1_rgb_data[i] = 0; // H
			//img1_rgb_data[i+1] = 255; // S
			//img1_rgb_data[i+2] = 0; // V

			img1_gray_data[j]=255;
		}
	}
}
