# Video Similarity
## Description
In this project, we take three descriptors into account for video similarity checking: Key point features, audio features, and color features. 

We use SIFT Feature Descriptor for Key point features mapping for each frame image; short-time energy and cosine similarity for audio feature checking; K- means, Distance formula between colors and Hausdorff distance for color similarity measure.

We conducted a formula ourselves by testing to calculate the final score combing all three features.

Final similarity score = (a*SIFT_Dist + (1-a) * Color_Dist) * Audio_Dist^(1/k), a=0.7 k=4
The smaller the final score, the more similar it means.

This repository provides code for video similarity check. Eclipse project files is provided. 

## Result & UI Interface
When the project runs, an UI interface will appears automatically. By clicking the "Select Movie" Botton, user can choose the query movie from file folder. 

<p align="center">
 <img src="VideoSimilarity/figure/UI_interface.jpg" height="600"/>
</p >

Then the project will calculate the final similarity distance, and provide three answers with the lowest score.(The lower the final score, the more similar it means.)

Here is an example that query video is exactly the same with one of the database videos clips.

<p align="center">
 <img src="VideoSimilarity/figure/exactmatch.jpg" height="600"/>
</p >

By dragging the progress bar or press "Play" button, the user can control video play. The user can also choose the other two videos shown in the list in the left top to play the video.


<p align="center">
 <img src="VideoSimilarity/figure/playmovie.jpg" height="600"/>
</p >

Here is an example that query video is exactly the same with one of the database videos clips.

<p align="center">
 <img src="VideoSimilarity/figure/inexactmatch.jpg" height="600"/>
</p >


