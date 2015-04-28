package com.moledetection.android;

import java.util.ArrayList;
import org.opencv.core.Mat;

public class ActiveContourDetection {
	char[] img_rgb_data;

	//! Red component of the current pixel.
    int R;
    //! Green component of the current pixel.
    int G;
    //! Blue component of the current pixel.
    int B;
    
    ArrayList<Integer> Lout,Lin = new ArrayList<Integer>();
    
    //! Image width, i.e. number of columns.
     int img_width;
    //! Image height, i.e. number of rows.
     int img_height;
    //! Image size, i.e. number of pixels. Obviously, it egals to #img_width × #img_height.
     int img_size;
    //! Pointer on the level-set function buffer.
    Character[]  phi;

    //! Mean of component \a Y of the pixels outside the curve, i.e. pixels \f$i\f$ with \f$\phi \left( i\right) >0\f$ .
    int CoutY;
    //! Mean of component \a U of the pixels outside the curve, i.e. pixels \f$i\f$ with \f$\phi \left( i\right) >0\f$ .
    int CoutU;
    //! Mean of component \a V of the pixels outside the curve, i.e. pixels \f$i\f$ with \f$\phi \left( i\right) >0\f$ .
    int CoutV;

    //! Mean of component \a Y of the pixels inside the curve, i.e. pixels \f$i\f$ with \f$\phi \left( i\right) <0\f$ .
    int CinY;
    //! Mean of component \a U of the pixels inside the curve, i.e. pixels \f$i\f$ with \f$\phi \left( i\right) <0\f$ .
    int CinU;
    //! Mean of component \a V of the pixels inside the curve, i.e. pixels \f$i\f$ with \f$\phi \left( i\right) <0\f$ .
    int CinV;

    //! Mean of component \a R of the pixels outside the curve, i.e. pixels \f$i\f$ with \f$\phi \left( i\right) >0\f$ .
    int CoutR;
    //! Mean of component \a G of the pixels outside the curve, i.e. pixels \f$i\f$ with \f$\phi \left( i\right) >0\f$ .
    int CoutG;
    //! Mean of component \a B of the pixels outside the curve, i.e. pixels \f$i\f$ with \f$\phi \left( i\right) >0\f$ .
    int CoutB;

    //! Mean of component \a R of the pixels inside the curve, i.e. pixels \f$i\f$ with \f$\phi \left( i\right) <0\f$ .
    int CinR;
    //! Mean of component \a G of the pixels inside the curve, i.e. pixels \f$i\f$ with \f$\phi \left( i\right) <0\f$ .
    int CinG;
    //! Mean of component \a B of the pixels inside the curve, i.e. pixels \f$i\f$ with \f$\phi \left( i\right) <0\f$ .
    int CinB;

    //! Weight of component \a Y to calculate external speed \a Fd.
     int alpha;
    //! Weight of component \a U to calculate external speed \a Fd.
     int beta;
    //! Weight of component \a V to calculate external speed \a Fd.
     int gamma;

    //! Weight of the outside homogeneity criterion in the Chan-Vese model.
     int lambda_out;
    //! Weight of the inside homogeneity criterion in the Chan-Vese model.
     int lambda_in;

    //! Sum of component #R of the pixels outside the curve, i.e. pixels \f$i\f$ with \f$\phi \left( i\right) >0\f$ .
    int sum_out_R;
    //! Sum of component #R of the pixels intside the curve, i.e. pixels \f$i\f$ with \f$\phi \left( i\right) <0\f$ .
    int sum_in_R;

    //! Sum of component #G of the pixels outside the curve, i.e. pixels \f$i\f$ with \f$\phi \left( i\right) >0\f$ .
    int sum_out_G;
    //! Sum of component #G of the pixels intside the curve, i.e. pixels \f$i\f$ with \f$\phi \left( i\right) <0\f$ .
    int sum_in_G;

    //! Sum of component #B of the pixels outside the curve, i.e. pixels \f$i\f$ with \f$\phi \left( i\right) >0\f$ .
    int sum_out_B;
    //! Sum of component #B of the pixels intside the curve, i.e. pixels \f$i\f$ with \f$\phi \left( i\right) <0\f$ .
    int sum_in_B;

    //! Number of pixels outside the curve, i.e. pixels \f$i\f$ with \f$\phi \left( i\right) >0\f$ .
    int n_out;
    //! Number of pixels inside the curve, i.e. pixels \f$i\f$ with \f$\phi \left( i\right) <0\f$ .
    int n_in;
    
  //! Boolean egals to \c true to have the curve smoothing, evolutions in the cycle 2 with the internal speed Fint.
     Boolean hasSmoothingCycle;
    //! Kernel length of the gaussian filter for the curve smoothing.
     int kernel_length;
    //! Standard deviation of the gaussian kernel for the curve smoothing.
     double sigma;
    //! Boolean egals to \c true to eliminate the redundant points in #Lin and #Lout lists.
     Boolean hasCleaning;

    //! Pointer on the gaussian kernel buffer used to calculate Fint.
     Integer[]  gaussian_kernel;

    //! Number of times the active contour evolves.
    int iterations;
    //! Maximum number of times the active contour can evolve.
     int max_iterations;
    //! Booleanean egals to \c true to stop the algorithm.
    Boolean hasAlgoStopping;

    //! List of points belong to the outside boundary.
    //ofeliList Lout;
    //! List of points belong to the inside boundary.
    //ofeliList Lin;

    //! \a kernel_radius = (  #kernel_length - 1) / 2 with #kernel_length impair.
     int kernel_radius;


    //! Boolean eguals to \c true if one point of #Lout at least is switched in during the scan through the #Lout list.
    Boolean hasOutwardEvolution;
    //! Boolean eguals to \c true if one point of #Lin at least is switched out during the scan through the #Lin list.
    Boolean hasInwardEvolution;

    //! Number of iterations the active contour evolves in the cycle 1 with \a Fd speed.
    int Na;
    //! Number of maximum iterations the active contour evolves in the cycle 1 with \a Fd speed.
     int Na_max;
    //! Number of iterations the active contour evolves in the cycle 2 with \a Fint speed.
    int Ns;
    //! Number of maximum iterations the active contour evolves in the cycle 2 with \a Fint speed.
     int Ns_max;
    //! Number of iterations in the last cycle 2.
    int Ns_last;

    //! Booleanean egals to true if #hasOutwardEvolution egals to \c true or #hasInwardEvolution egals to \c true.
    Boolean hasListsChanges;
    //! Length of #Lin + length of #Lout.
    int lists_length;
    //! Length of #Lin + length of #Lout of the previous iteration.
    int previous_lists_length;
    //! Booleanean egals to \c true if oscillations are detected. In this case, #hasAlgoStopping egals to \c true
    Boolean hasOscillation;
    //! To count the number of times the relative gap of the lists length are less of a ant, fixed at 0.5%.
    int counter;
    //! Booleanean egals to \c false to go away the cycle 1 and never to go back into.
    Boolean hasCycle1;
    
    public ActiveContourDetection(char[] img_rgb_data1, int img_width1, int img_height1, Boolean hasEllipse1, double init_width1, double init_height1, double center_x1, double center_y1, Boolean hasSmoothingCycle1, int kernel_length1, double sigma1, Boolean hasCleaning1, int Na1, int Ns1, int lambda_out1, int lambda_in1, int alpha1, int beta1, int gamma1) {
    	this.img_width = img_width1;
    	this.img_height = img_height1;
    	this.hasSmoothingCycle = hasSmoothingCycle1;
    	this.kernel_length = kernel_length1;
    	this.sigma =sigma1;
    	this.hasCleaning = hasCleaning1;
    	this.Na_max = Na1;
    	this.Ns_max = Ns1;
    	this.img_size = img_width1 * img_height1;
    	this.kernel_radius = (kernel_length1 - 1)/2;
    	this.phi = new Character[img_width1 * img_height1];
    	this.img_rgb_data = img_rgb_data1;
    	make_gaussian_kernel(kernel_length1, sigma1);
    	calculate_max_iterations(img_width1, img_height1);
    	initialize_sums();
        calculate_means();
        // active contour initialization with phi, Lout and Lin

        int x, y; // position of the current pixel

        // do an ellipse
        if( hasEllipse1 )
        {
            for( int i = 0; i < img_size; i++ )
            {
                // i = x+y*img_width so
                y = i/img_width;
                x = i-y*img_width;

                // ellipse inequation
                if( square( (double)y-(1.0+2.0*center_y1)*(double)img_height/2.0 ) / square( init_height1*(double)img_height/2.0 ) + square( (double)x-(1.0+2.0*center_x1)*(double)img_width/2.0 ) / square( init_width1*(double)img_width/2.0 ) > 1.0 )
                {
                    phi[i] = 1; // outside
                    Lout.add(i);
                }
                else
                {
                    phi[i] = (char) -1; // inside
                    Lin.add(i);
                }
            }
        }

        // do a rectangle
        else
        {
            for( int i = 0; i < img_size; i++ )
            {
                // i = x+y*img_width so
                y = i/img_width;
                x = i-y*img_width;

                if( (double)y > ((1.0-init_height1)*(double)img_height/2.0 + center_y1*(double)img_height) && (double)y < (((double)img_height-(1.0-init_height1)*(double)img_height/2.0) + center_y1*(double)img_height) && (double)x > ((1.0-init_width1)*(double)img_width/2.0 + center_x1*(double)img_width) && (double)x < (((double)img_width-(1.0-init_width1)*(double)img_width/2.0) + center_x1*(double)img_width) )
                {
                    phi[i] = (char) -1; // inside
                    Lin.add(i);
                }
                else
                {
                    phi[i] = 1; // outside
                    Lout.add(i);
                }
            }
        }

        eliminate_redundant_in_Lout(); // clean Lout boundary
        eliminate_redundant_in_Lin(); // clean Lin boundary

        // variables initialization
        initialize();
	}

	private void initialize_sums() {
	    sum_in_R = 0;
	    sum_out_R = 0;

	    sum_in_G = 0;
	    sum_out_G = 0;

	    sum_in_B = 0;
	    sum_out_B = 0;

	    n_in = 0;
	    n_out = 0;

	    for( int i = 0; i < img_size; i++ )
	    {
	        // RGB planar image data buffer, with R1 R2 R3 ... G1 G2 G3 ... B1 B2 B3 ... (for Matlab)
	        /*
	        R = int(img_rgb_data[ i ]);
	        G = int(img_rgb_data[ i+img_size ]);
	        B = int(img_rgb_data[ i+2*img_size ]);
	        */

	        // BGR interleaved image data buffer, with B1 G1 R1 B2 G2 R2 ...
	        B = (int)img_rgb_data[ 3*i ];
	        G = (int)img_rgb_data[ 3*i+1 ];
	        R = (int)img_rgb_data[ 3*i+2 ];

	        if( phi[i] > 0 )
	        {
	            sum_out_R += R;
	            sum_out_G += G;
	            sum_out_B += B;

	            n_out++;
	        }
	        else
	        {
	            sum_in_R += R;
	            sum_in_G += G;
	            sum_in_B += B;

	            n_in++;
	        }
	    }

	    return;
		
	}

	private void calculate_means() {
		// protection against /0
	    if( n_out == 0 )
	    {
	        CoutR = 0;
	        CoutG = 0;
	        CoutB = 0;
	    }
	    else
	    {
	        CoutR = sum_out_R/n_out;
	        CoutG = sum_out_G/n_out;
	        CoutB = sum_out_B/n_out;
	    }

	    // protection against /0
	    if( n_in == 0 )
	    {
	        CinR = 255;
	        CinG = 255;
	        CinB = 255;
	    }
	    else
	    {
	        CinR = sum_in_R/n_in;
	        CinG = sum_in_G/n_in;
	        CinB = sum_in_B/n_in;
	    }

	    CoutY = calculate_Y(CoutR, CoutG, CoutB);
	    CoutU = calculate_U(CoutR, CoutG, CoutB);
	    CoutV = calculate_V(CoutR, CoutG, CoutB);

	    CinY = calculate_Y(CinR, CinG, CinB);
	    CinU = calculate_U(CinR, CinG, CinB);
	    CinV = calculate_V(CinR, CinG, CinB);

	    return;
		
	}
	
	 private int calculate_Y(int R, int G, int B)
	{
	    return ( (  66 * R + 129 * G +  25 * B + 128) >> 8) +  16;
	}

	 private int calculate_U(int R, int G, int B)
	{
	    return ( ( -38 * R -  74 * G + 112 * B + 128) >> 8) + 128;
	}

	 private int calculate_V(int R, int G, int B)
	{
	    return ( ( 112 * R -  94 * G -  18 * B + 128) >> 8) + 128;
	}
	 
	 private int compute_external_speed_Fd(int i, int x, int y)
	 {
	     // RGB planar image data buffer, with R1 R2 R3 ... G1 G2 G3 ... B1 B2 B3 ... (for Matlab)
	     /*
	     R = int(img_rgb_data[ i ]);
	     G = int(img_rgb_data[ i+img_size ]);
	     B = int(img_rgb_data[ i+2*img_size ]);
	     */

	     // BGR interleaved image data buffer, with B1 G1 R1 B2 G2 R2 ...
		 B = (int)img_rgb_data[ 3*i ];
	        G = (int)img_rgb_data[ 3*i+1 ];
	        R = (int)img_rgb_data[ 3*i+2 ];

	     int Y = calculate_Y(R, G, B);
	     int U = calculate_U(R, G, B);
	     int V = calculate_V(R, G, B);

	     return (int) (lambda_out*( alpha*square(Y-CoutY)+beta*square(U-CoutU)+gamma*square(V-CoutV) ) - lambda_in*( alpha*square(Y-CinY)+beta*square(U-CinU)+gamma*square(V-CinV) ));
	 }
	 
	 private double square(double value) {
		 return value*value; 
		 }

	 private void do_specific_in_step(int i, int x, int y)
	 {
	     sum_out_R -= R;
	     sum_out_G -= G;
	     sum_out_B -= B;
	     n_out--;

	     sum_in_R += R;
	     sum_in_G += G;
	     sum_in_B += B;
	     n_in++;

	     return;
	 }

	 private void do_specific_out_step(int i, int x, int y)
	 {
	     sum_in_R -= R;
	     sum_in_G -= G;
	     sum_in_B -= B;
	     n_in--;

	     sum_out_R += R;
	     sum_out_G += G;
	     sum_out_B += B;
	     n_out++;

	     return;
	 }

	 private void do_specific_in_step2(int i, int x, int y)
	 {
	     // RGB planar image data buffer, with R1 R2 R3 ... G1 G2 G3 ... B1 B2 B3 ... (for Matlab)
	     /*
	     R = int(img_rgb_data[ i ]);
	     G = int(img_rgb_data[ i+img_size ]);
	     B = int(img_rgb_data[ i+2*img_size ]);
	     */

	     // BGR interleaved image data buffer, with B1 G1 R1 B2 G2 R2 ...
		 B = (int)img_rgb_data[ 3*i ];
	        G = (int)img_rgb_data[ 3*i+1 ];
	        R = (int)img_rgb_data[ 3*i+2 ];

	     sum_out_R -= R;
	     sum_out_G -= G;
	     sum_out_B -= B;
	     n_out--;

	     sum_in_R += R;
	     sum_in_G += G;
	     sum_in_B += B;
	     n_in++;

	     return;
	 }

	 private void do_specific_out_step2(int i, int x, int y)
	 {
	     // RGB planar image data buffer, with R1 R2 R3 ... G1 G2 G3 ... B1 B2 B3 ... (for Matlab)
	     /*
	     R = int(img_rgb_data[ i ]);
	     G = int(img_rgb_data[ i+img_size ]);
	     B = int(img_rgb_data[ i+2*img_size ]);
	     */

	     // BGR interleaved image data buffer, with B1 G1 R1 B2 G2 R2 ...
		 B = (int)img_rgb_data[ 3*i ];
	        G = (int)img_rgb_data[ 3*i+1 ];
	        R = (int)img_rgb_data[ 3*i+2 ];

	     sum_in_R -= R;
	     sum_in_G -= G;
	     sum_in_B -= B;
	     n_in--;

	     sum_out_R += R;
	     sum_out_G += G;
	     sum_out_B += B;
	     n_out++;

	     return;
	 }

	 void do_specific_beginning_step()
	 {
	     calculate_means();

	     return;
	 }
	 
	 int get_CoutR() 
	 {
	     return CoutR;
	 }
	 int get_CoutG() 
	 {
	     return CoutG;
	 }
	 int get_CoutB() 
	 {
	     return CoutB;
	 }

	 int get_CinR() 
	 {
	     return CinR;
	 }
	 int get_CinG() 
	 {
	     return CinG;
	 }
	 int get_CinB() 
	 {
	     return CinB;
	 }


	 int get_CoutY() 
	 {
	     return CoutY;
	 }
	 int get_CoutU() 
	 {
	     return CoutU;
	 }
	 int get_CoutV() 
	 {
	     return CoutV;
	 }

	 int get_CinY() 
	 {
	     return CinY;
	 }
	 int get_CinU() 
	 {
	     return CinU;
	 }
	 int get_CinV() 
	 {
	     return CinV;
	 }
	 
	 void initialize()
	 {
	     // initialization of 4 variables for the oscillation stopping condition
	     previous_lists_length = 999999;
	     lists_length = 0;
	     counter = 0;

	     do_general_initialization_for_each_frame();

	     return;
	 }

	 void do_general_initialization_for_each_frame()
	 {
	     Na = 0; // in the cycle 1
	     Ns = 0; // in the cycle 2
	     Ns_last = 0; // in the last cycle 2
	     hasCycle1 = true;

	     // 3 variables of 3 stopping conditions
	     hasListsChanges = true;
	     hasOscillation = false;
	     iterations = 0;

	     hasAlgoStopping = false; // to start the algorithm

	     return;
	 }
	 

 Integer[]  make_gaussian_kernel(int kernel_length1, double sigma1)
{
    // kernel_length impair and strictly positive
    if( kernel_length1 % 2 == 0 )
    {
        kernel_length1--;
    }

    if( kernel_length1 < 1 )
    {
        kernel_length1 = 1;
    }

    // protection against /0
    if( sigma1 < 0.000000001 )
    {
        sigma1 = 0.000000001;
    }

    int x, y; // position of the current pixel

     int kernel_size1 = kernel_length1*kernel_length1;
     int kernel_radius1 = (kernel_length1-1)/2;

    Integer[]  gaussian_kernel1 = new Integer[kernel_size1];

    for( int i = 0; i < kernel_size1; i++ )
    {
        // i = x+y*kernel_length so
        y = i/kernel_length1;
        x = i-y*kernel_length1;

        gaussian_kernel1[i] = (int) ((int) 0.5+100000.0*Math.exp( -( square((double)y-(double)kernel_radius1) + square((double)x-(double)kernel_radius1) ) / (2*square(sigma1)) )) ;
    }

    return gaussian_kernel1;
}

void do_one_iteration_in_cycle1()
{
    int i, x, y; // offset and position of the current point of the active contour

    hasOutwardEvolution = false;

    do_specific_beginning_step(); // to calculate Cout and Cin means in O(1) in Chan-Vese model.

    // scan through Lout
    for( int i1 = Lout.size();i1 >= 0 ; i1--  )
    {
        i = Lout.get(i1 - 1);

        // i = x+y*img_width so
        y = i/img_width;
        x = i-y*img_width;

        if( compute_external_speed_Fd(i,x,y) > 0 )
        {
            do_specific_in_step(i,x,y); // updates of the variables to calculate the means Cout and Cin

            switch_in(i,x,y); // outward local movement

            if( !hasOutwardEvolution )
            {
                hasOutwardEvolution = true;
            }
        }
    }

    if( hasOutwardEvolution && hasCleaning )
    {
        eliminate_redundant_in_Lin(); // Lin boundary cleaning
    }

    hasInwardEvolution = false;

    // scan through Lin
    for( int i1 = Lin.size();i1 >= 0 ; i1--  )
    {
        i = Lin.get(i1 - 1);

        // i = x+y*img_width so
        y = i/img_width;
        x = i-y*img_width;

        if( compute_external_speed_Fd(i,x,y) < 0 )
        {
            do_specific_out_step(i,x,y); // updates of the variables to calculate the means Cout and Cin

            switch_out(i,x,y); // inward local movement

            if( !hasInwardEvolution )
            {
                hasInwardEvolution = true;
            }
        }
    }

    if( hasInwardEvolution && hasCleaning )
    {
        eliminate_redundant_in_Lout(); // Lout boundary cleaning
    }

    calculate_stopping_conditions1();

    return;
}

void do_one_iteration_in_cycle2()
{
    int i, x, y; // offset and position of the current point of the active contour

    // scan through Lout
    for( int i1 = Lout.size();i1 >= 0 ; i1--  )
    {
        i = Lout.get(i1 - 1);

        // i = x+y*img_width so
        y = i/img_width;
        x = i-y*img_width;

        if( Ns == Ns_max-1 )
        {
            lists_length++;
        }

        if( compute_internal_speed_Fint(i,x,y) < 0 )
        {
            do_specific_in_step2(i,x,y); // updates of the variables to calculate the means Cout and Cin

            switch_in(i,x,y); // outward local movement
        }
    }

    if( hasCleaning )
    {
        eliminate_redundant_in_Lin(); // Lin boundary cleaning
    }

    // scan through Lin
    for( int i1 = Lin.size();i1 >= 0 ; i1--  )
    {
        i = Lin.get(i1 - 1);

        // i = x+y*img_width so
        y = i/img_width;
        x = i-y*img_width;

        if( Ns == Ns_max-1 )
        {
            lists_length++;
        }

        if( compute_internal_speed_Fint(i,x,y) > 0 )
        {
            do_specific_out_step2(i,x,y); // updates of the variables to calculate the means Cout and Cin

            switch_out(i,x,y); // inward local movement
        }
    }

    if( hasCleaning )
    {
        eliminate_redundant_in_Lout(); // Lout boundary cleaning
    }

    iterations++;

    return;
}

public void evolve_one_iteration()
{
    if( !hasAlgoStopping )
    {
        // Na_max iterations in the cycle 1
        if( hasCycle1 )
        {
            if( Na < Na_max-1 )
            {
                do_one_iteration_in_cycle1(); // data dependant evolution
                Na++;
                return;
            }

            // last iteration of the cycle 1
            if( Na == Na_max-1 )
            {
                do_one_iteration_in_cycle1(); // data dependant evolution

                if( hasSmoothingCycle )
                {
                    Na++; // in order to do the cycle2, now Na = Na_max
                }
                else
                {
                    Na = 0; // in order to restart the cycle1
                }

                return;
            }
        }

        // Ns_max iterations in the cycle 2
        if( hasSmoothingCycle )
        {
            if( Ns < Ns_max-1 )
            {
                do_one_iteration_in_cycle2(); // ac regularization
                Ns++;
                return;
            }

            // last iteration of the cycle 2
            if( Ns == Ns_max-1 )
            {
                do_one_iteration_in_cycle2(); // ac regularization
                calculate_stopping_conditions2();
                // in order to restart the both cycles
                Na = 0;
                Ns = 0;
                return;
            }
        }
    }

    return;
}

public void evolve_n_iterations(int n)
{
     int N = iterations+n;

    while( !hasAlgoStopping && iterations < N )
    {
        for(  ; Na < Na_max && hasCycle1 && iterations < N; Na++ )
        {
            do_one_iteration_in_cycle1();
        }
        Na = 0;

        if( hasSmoothingCycle )
        {
            for(  ; Ns < Ns_max && iterations < N; Ns++ )
            {
                do_one_iteration_in_cycle2();
            }
            Ns = 0;
            calculate_stopping_conditions2();
        }
    }

    return;
}

public void evolve_to_final_state()
{
    while( !hasAlgoStopping )
    {
        for(  ; Na < Na_max && hasCycle1; Na++ )
        {
            do_one_iteration_in_cycle1();
        }
        Na = 0;

        if( hasSmoothingCycle )
        {
            for(  ; Ns < Ns_max; Ns++ )
            {
                do_one_iteration_in_cycle2();
            }
            Ns = 0;

            calculate_stopping_conditions2();
        }
    }

    return;
}

private void switch_in(int i, int x, int y)
{
    // Outward local movement

    // current point (x,y) of Lout is switched in Lin
    //Lin.switch_current(Lout);
	Lin.add(i);
	Lout.remove(i);
    // <==> Lout.erase(); Lin.push_front(i); but without use of operator delete and operator new.

    phi[i] = (char) -1; // value is changed from 1 to -1


    //==============   4-connex version   =============

    if( y-1 >= 0 )
    {
        add_Rout_neighbor_to_Lout( x+(y-1)*img_width );
    }
    if( x-1 >= 0 )
    {
        add_Rout_neighbor_to_Lout( (x-1)+y*img_width );
    }
    if( x+1 < img_width )
    {
        add_Rout_neighbor_to_Lout( (x+1)+y*img_width );
    }
    if( y+1 < img_height )
    {
        add_Rout_neighbor_to_Lout( x+(y+1)*img_width );
    }
    return;
}

private void add_Rout_neighbor_to_Lout(int i_neighbor)
{
    // if a neighbor ∈ Rout
    if( phi[i_neighbor] == 3 )
    {
        phi[i_neighbor] = 1;

        // neighbor ∈ Rout --> ∈ Lout
        Lout.add(i_neighbor);
    }

    return;
}

private void switch_out(int i, int x, int y)
{
    // Inward local movement

    // current point (x,y) of Lin is switched in Lout
    Lout.add(i);
    Lin.remove(i);
    // <==> Lin.erase(); Lout.push_front(i); but without use of operator delete and operator new.

    phi[i] = 1; // value is changed from -1 to 1


    if( y-1 >= 0 )
    {
        add_Rin_neighbor_to_Lin( x+(y-1)*img_width );
    }
    if( x-1 >= 0 )
    {
        add_Rin_neighbor_to_Lin( (x-1)+y*img_width );
    }
    if( x+1 < img_width )
    {
        add_Rin_neighbor_to_Lin( (x+1)+y*img_width );
    }
    if( y+1 < img_height )
    {
        add_Rin_neighbor_to_Lin( x+(y+1)*img_width );
    }

    return;
}

private void add_Rin_neighbor_to_Lin(int i_neighbor)
{
    // if a neighbor ∈ Rin
    if( phi[i_neighbor] == -3 )
    {
        phi[i_neighbor] = (char) -1;

        // neighbor ∈ Rin --> ∈ Lin
        Lin.add(i_neighbor);
    }

    return;
}

private int compute_internal_speed_Fint(int i, int x, int y)
{
    int Fint = 0;

    // if not in the border of the image, no neighbor test
    if( x > kernel_radius-1 && x < img_width-kernel_radius && y > kernel_radius-1 && y < img_height-kernel_radius )
    {
        for( int dy = -kernel_radius; dy <= kernel_radius; dy++ )
        {
            for( int dx = -kernel_radius; dx <= kernel_radius; dx++ )
            {
                Fint += gaussian_kernel[ (kernel_radius+dx)+(kernel_radius+dy)*kernel_length ]*signum_function( phi[(x+dx)+(y+dy)*img_width] );
            }
        }
    }
    // if in the border of the image, tests of neighbors
    else
    {
        for( int dy = -kernel_radius; dy <= kernel_radius; dy++ )
        {
            for(int dx = -kernel_radius; dx <= kernel_radius; dx++ )
            {
                if( x+dx >= 0 && x+dx < img_width && y+dy >= 0 && y+dy < img_height )
                {
                    Fint += gaussian_kernel[ (kernel_radius+dx)+(kernel_radius+dy)*kernel_length ]*signum_function( phi[(x+dx)+(y+dy)*img_width] );
                }
                else
                {
                    Fint += gaussian_kernel[ (kernel_radius+dx)+(kernel_radius+dy)*kernel_length ]*signum_function( phi[i] );
                }
            }
        }
    }

    return Fint;
}

private int signum_function(char i)
{
    if( i < 0 )
    {
        return -1;
    }
    else
    {
        return 1;
    }
}

private Boolean find_isRedundantPointOfLin(int x, int y) 
{
    //==============   4-connex version   =============

    // if ∃ a neighbor ∈ Lout | ∈ Rout
    if( y-1 >= 0 )
    {
        if( phi[ x+(y-1)*img_width ] >= 0 )
        {
            return false; // is not redundant point of Lin
        }
    }
    if( x-1 >= 0 )
    {
        if( phi[ (x-1)+y*img_width ] >= 0 )
        {
            return false; // is not redundant point of Lin
        }
    }
    if( x+1 < img_width )
    {
        if( phi[ (x+1)+y*img_width ] >= 0 )
        {
            return false; // is not redundant point of Lin
        }
    }
    if( y+1 < img_height )
    {
        if( phi[ x+(y+1)*img_width ] >= 0 )
        {
            return false; // is not redundant point of Lin
        }
    }
    return true; // is redundant point of Lin
}

private Boolean find_isRedundantPointOfLout(int x, int y) 
{
    //==============   4-connex version   =============

    // if ∃ a neighbor ∈ Lin | ∈ Rin
    if( y-1 >= 0 )
    {
        if( phi[ x+(y-1)*img_width ] <= 0 )
        {
            return false; // is not redundant point of Lout
        }
    }
    if( x-1 >= 0 )
    {
        if( phi[ (x-1)+y*img_width ] <= 0 )
        {
            return false; // is not redundant point of Lout
        }
    }
    if( x+1 < img_width )
    {
        if( phi[ (x+1)+y*img_width ] <= 0 )
        {
            return false; // is not redundant point of Lout
        }
    }
    if( y+1 < img_height )
    {
        if( phi[ x+(y+1)*img_width ] <= 0 )
        {
            return false; // is not redundant point of Lout
        }
    }
	return true;
}

void eliminate_redundant_in_Lin()
{
    int i, x, y; // offset and position of the current point of the active contour

    // scan through Lin
    for( int i1 = Lin.size();i1 >= 0 ; i1--  )
    {
        i = Lin.get(i1 - 1);

        // i = x+y*img_width so
        y = i/img_width;
        x = i-y*img_width;

        // if ∀ neighbors ∈ Lin | ∈ Rin
        if( find_isRedundantPointOfLin(x,y) )
        {
            phi[i] = (char) -3; // phi(point) = -1 --> -3
            Lin.remove(i1 - 1); // point ∈ Lin --> ∈ Rin
        }
    }

    return;
}

void eliminate_redundant_in_Lout()
{
    int i, x, y; // offset and position of the current point of the active contour

    // scan through Lout
    for( int i1 = Lout.size();i1 >= 0 ; i1--  )
    {
        i = Lout.get(i1 - 1);

        // i = x+y*img_width so
        y = i/img_width;
        x = i-y*img_width;

        // if ∀ neighbors ∈ Lout | ∈ Rout
        if( find_isRedundantPointOfLout(x,y) )
        {
            phi[i] = 3; // phi(point) = 1 --> 3
            Lout.remove(i1 - 1); // point ∈ Lout --> ∈ Rout
        }
    }

    return;
}

// at the end of each iteration in the cycle 1
void calculate_stopping_conditions1()
{
    if( !hasInwardEvolution && !hasOutwardEvolution )
    {
        hasListsChanges = false;
    }

    iterations++;

    if( !hasListsChanges || iterations >= max_iterations )
    {
        hasCycle1 = false;

        if( !hasSmoothingCycle )
        {
            hasAlgoStopping = true;
        }
    }

    return;
}

// at the end of the last iteration of the cycle 2
void calculate_stopping_conditions2()
{
    // if the relative difference of active contour length between two cycle2 is less of 0.01
    if( (double)Math.abs(previous_lists_length-lists_length)/(double)Math.max(lists_length,previous_lists_length) < 0.01 )
    {
        counter++;
    }
    // not consecutivly ==> reinitialization
    else
    {
        if( counter != 0 )
        {
            counter = 0;
        }
    }

    // if 3 times consecutivly
    if( counter == 3 )
    {
        hasOscillation = true;
    }

    if( hasOscillation || iterations >= max_iterations || !hasCycle1 )
    {
        hasAlgoStopping = true;
    }

    // keep last length to compare after
    previous_lists_length = lists_length;

    // reinitialization
    lists_length = 0;

    return;
}

private int calculate_max_iterations(int width1, int height1)
{
    if( width1 > height1 )
    {
        return 5*height1;
    }
    else
    {
        return 5*width1;
    }
}

// template functions square is defined in the header file

Character[]  get_phi() 
{
    return phi;
}

 ArrayList<Integer> get_Lout() 
{
    return Lout;
}

 ArrayList<Integer> get_Lin() 
{
    return Lin;
}



// 5 getters to see the variables of the stopping conditions
Boolean get_hasListsChanges() 
{
    return hasListsChanges;
}

Boolean get_hasOscillation() 
{
    return hasOscillation;
}

int get_iterations() 
{
    return iterations;
}

int get_max_iterations() 
{
    return max_iterations;
}

Boolean get_hasAlgoStopping() 
{
    return hasAlgoStopping;
}

}
