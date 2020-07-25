;Bones

;go into graphics mode
Graphics 640,480
Global width=GraphicsWidth()
Global height=GraphicsHeight()
Global midx=width/2
Global midy=height/2
Global areamin=-2048; Lower x boundary of playing area. in pixels
Global areamax=2048;  Upperer x boundary of playing area. in pixels


;Sets some horizontal coordinates and stores in H(n)
steps#=20
Dim H(steps#)
For loop#=1.00 To steps#
H(loop#)=(loop#/steps#)*height
Next

;Sets some vertical coordinates and stores in V(n)
steps#=20
Dim V(steps#)
For loop#=1.00 To steps#
V(loop#)=(loop#/steps#)*width
Next



;Load Graphics
;Tells Blitz Basic  to handle centring of images. (Important for rotation of images)
AutoMidHandle True 
Global im_ship_r=LoadImage("data\defender_ship_x2a.bmp")
Global im_ship_l=LoadImage("data\defender_ship_x2b.bmp")
Global im_city=LoadImage("data\city1.bmp")

;**********************
;* player paramenters *
;**********************

Global player_x=V(2)
Global player_y=H(14)
Global player_dx ;x velocity
Global player_dy ;y velocity
Global player_f=1 ;Direction ship is facing
shipwidth=ImageWidth(im_ship_r)    ; width of ship image
shipheight=ImageHeight(im_ship_r)	 ; height of ship image
Global shipymax=height-(shipheight/2); maximum y-coord of ship
Global shipymin=shipheight/2


;*********************
;* Baddie Parameters *
;*********************

Global nbem=0; Number of baddies
Global shift=0; Shift parameter that ensures baddies remain at same relative positions to ship when it reverses

;Data type for baddie
Type blob
Field x  ;x
Field y  ;y
Field dx ;x velocity
Field dy ;y velocity
Field im ;image type
Field mt ;movement type
Field hp ;hit points
End Type

;City parameters
Type town
Field x ;x
Field y ;y
Field s ;Status (alive/dead)
End Type
Global citywidth=ImageWidth(im_city)    ; width of ship image
Global cityheight=ImageHeight(im_city)	 ; height of ship image


;Skyline parameters
Type point
Field x
Field y
End Type


;enable double buffering
SetBuffer BackBuffer()


set_baddies()
set_cities()
set_skyline()
;loop until ESC hit.
While Not KeyDown(1)
Cls
Text 100,100,player_f
Text 100,120,player_dx
update_ship()
update_baddies()
update_skyline()
update_cities()
control_ship()

Flip 
Wend
End

;*********************************************************************
;*** FUNCTIONS START HERE ********************************************
;*********************************************************************


Function set_baddies()
For loop=1 To 50
bem.blob=New blob
bem\x=Rnd(areamin,areamax)
bem\y=Rnd(0,height)
Next
End Function

Function set_cities()
For loop=1 To 6
city.town=New town
city\x=-300-citywidth+(loop*100)
city\y=H(18)-(cityheight/2)
Next
End Function 


;Creates Random Hilly Skyline
;- direction is a mirror image of the + direction
; this is a lazy way to ensure there is no disconinuity at the wrap point.
Function set_skyline()
y=H(18)
dy=Rnd(2,-2)
For loop = 0 To areamax
k=Rnd(3):If k=3 Then dy=Rnd(2,-2)
hill.point=New point;Poistive direction from origin
hill\x=loop
hill\y=y
hill.point=New point;Negaive direction from origin
hill\x=-loop
hill\y=y
y=y+dy
If y<H(14) Then y=H(14):dy=3; Restricts height of mountains
If y>height Then y=height:dy=-3; Restricts depth of vallies
If loop<300 Then y=H(18); Forces flat area for cities to be placed
Next
End Function 

Function update_baddies()
For bem.blob = Each blob
;updates position relative to ship based on ship's x movement.
bem\x=move_rel(bem\x)
Color Rnd(255),Rnd(255),255
Oval bem\x,bem\y,5,5
Next
End Function

Function update_cities()
For city.town = Each town
;updates position relative to ship based on ship's x movement.
city\x=move_rel(city\x)
DrawImage im_city,city\x,city\y
Next
End Function


Function update_skyline()
Color 255,0,0
For hill.point = Each point
hill\x=move_rel(hill\x)
Plot hill\x,hill\y
Next
End Function


;Updates objects x-position relative to the ship based on the ships "motion".
Function move_rel(x)
delta=player_dx+shift 
x=x+delta
If x<areamin Then d=areamin-x:x=areamax-d
If x>areamax Then d=x-areamax:x=areamin+d
Return x
End Function


;Update ship position
Function update_ship()
shift=0
If player_f=1 And player_x>V(2) Then player_x=player_x-4:shift=-4
If player_f=-1 And player_x<V(18) Then player_x=player_x+4:shift=4

player_y=player_y+player_dy
If player_y<shipymin Then player_y= shipymin
If player_y>shipymax Then player_y= shipymax

If player_f=1 Then 
DrawImage(im_ship_r,player_x,player_y)
Else DrawImage(im_ship_l,player_x,player_y)
End If  
End Function 


;Player controlls for ship
Function control_ship()
player_dy=0

;Accelerates ship in the direction it is facing
If JoyDown(3) Or KeyDown(54) Then 
player_dx=player_dx-(1*player_f)
End If
If player_dx>16 Then player_dx=16
If player_dx<-16 Then player_dx=-16

;If ; pressed then move ship up
If KeyDown(30) Or JoyYDir()=-1 Then player_dy=-8
;If . pressed then move ship right
If KeyDown(44) Or JoyYDir()=1 Then player_dy=8
;Changes ship direction if joystick moved left or right
If JoyXDir()=-1 Then player_f=-1
If JoyXDir()=1 Then player_f=1
;Reverses ship if space bar hit
If KeyHit(57) Then player_f=-player_f
End Function 