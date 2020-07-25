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
Global im_bigast1=LoadImage("data\bigast1.bmp")
Global im_bigast2=LoadImage("data\bigast2.bmp")
Global im_bigast3=LoadImage("data\bigast3.bmp")

Global im_asteroid=LoadImage("data\asteroid.bmp")
Global im_defender=LoadImage("data\defender1.bmp")
Global im_command=LoadImage("data\command.bmp")




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
Field dy# ;y velocity
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


;************************
;*** Laser Parameters ***
;************************
;
Global nbeams=0    ;Number of beams in existence
Global maxbeams=4  ;Maximum number of symaltaneous beams allowed
Global lascol=1    ;lasercolour
;

;Horizontal laser beam
Type laser
Field y
Field x   ; Start point of beam
Field x2  ; end point of beam
Field xmax; maximum end point of beam
Field v   ; x-velocity of ship at time of firing laser
Field d   ; direction (1 = left, -1=right)
Field c   ; colour
End Type 



;Skyline parameters
Type point
Field x
Field y
Field s; does point show on the scanner
End Type


;enable double buffering
SetBuffer BackBuffer()

greet()
set_baddies()
set_cities()
set_skyline()
;loop until ESC hit.
While Not KeyDown(1)
Cls

update_ship()
update_laser()
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
For loop=1 To 10
bem.blob=New blob
bem\x=Rnd(areamin,areamin+600)
bem\y=Rnd(H(2),H(15))
bem\dx=2-Rnd(4)
bem\dy#=1
bem\im=Rnd(1,3)
bem\hp=1 ; Hit point(s)
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
For loop = 0 To areamax Step 2
If (loop Mod(9))=1 Then scanon=1 Else scanon=0
k=Rnd(2):If k=2 Then dy=Rnd(2,-2)
hill.point=New point;Poistive direction from origin
hill\x=loop
hill\y=y
If scanon=1 Then hill\s=True Else hill\s=False
hill.point=New point;Negaive direction from origin
hill\x=-loop
hill\y=y
If scanon=1 Then hill\s=True Else hill\s=False
y=y+dy
If y<H(13) Then y=H(13):dy=2; Restricts height of mountains
If y>=(height-2) Then y=height-2:dy=-2; Restricts depth of vallies
If loop<300 Then y=H(18); Forces flat area for cities to be placed
Next
End Function 

Function update_baddies()
For bem.blob = Each blob
bem\x=bem\x+bem\dx
bem\y=bem\y+bem\dy#

;Stops baddies from leaving top or bottom of screen
bem\dy=confine#(bem\y,bem\dy,0,height)

;updates position relative to ship based on ship's x movement.
bem\x=move_rel(bem\x)
im_temp=assign_img(bem\im)
DrawImage im_temp,bem\x,bem\y
scanner(bem\x,bem\y,1,2)
If bem\hp=0 Then Delete bem
Next
End Function

;Selects appropriate image for each baddie
Function assign_img(n)
Select n
Case 1; Spectrun Logo (rotating)
im_temp=im_bigast1
Case 2; Prism (rotating)
im_temp=im_bigast2
Case 3; Soup
im_temp=im_bigast3
Default 
im_temp=im_bigast1
End Select
Return im_temp
End Function  


Function update_cities()
For city.town = Each town
;updates position relative to ship based on ship's x movement.
city\x=move_rel(city\x)
DrawImage im_city,city\x,city\y
scanner(city\x,city\y,4,2)
Next
End Function


Function update_skyline()
col(6)
For hill.point = Each point
hill\x=move_rel(hill\x)
Rect hill\x,hill\y,2,2
If hill\s=True Then scanner(hill\x,hill\y,6,1)
Next
End Function


;Updates objects x-position relative to the ship based on the ships "motion".
;Needed to for the moving viewpoint of the scrolling screen to work
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
scanner(player_x,player_y,7,3)
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
If KeyDown(30) Or JoyYDir()=-1 Then player_dy=-6
;If . pressed then move ship right
If KeyDown(44) Or JoyYDir()=1 Then player_dy=6
;Changes ship direction if joystick moved left or right
If JoyXDir()=-1 Then player_f=-1
If JoyXDir()=1 Then player_f=1
;Reverses ship if space bar hit
If KeyHit(57) Then player_f=-player_f

;If fire button pressed fire laser if maximum number not already reached 
If (KeyHit(28) Or JoyHit(1)) And nbeams<maxbeams Then shoot_laser(player_x,player_y,480,player_dx)
End Function 


;***********************
;*** Laser Functions ***
;***********************

;Initialises new beam
Function shoot_laser(x,y,l,v)
nbeams=nbeams+1
pop.laser=New laser
pop\y=y
pop\x=x
pop\x2=x
pop\xmax=pop\x+(l*player_f)
pop\d=player_f
pop\v=v
pop\c=lascol
End Function

;Updates beam.
Function update_laser()
For pop.laser = Each laser
col(pop\c)

;The function move_rel corrects position to take moving viewpoint into account.
; pop\v is subtracted to take adjust for the velocity of the ship at the time the laser was fired.
pop\x=move_rel(pop\x-pop\v)
pop\x2=move_rel(pop\x2-pop\v)
pop\xmax=move_rel(pop\xmax-pop\v)

Line pop\x,pop\y,pop\x2,pop\y

;Adds speckles to beam
Color 0,0,0
For loop=1To 10
x=Rnd(pop\x,pop\x2)
Line  x,pop\y,x+4,pop\y
Next

;Sets movement rates for updward (pop\d=1) and downward moving lasers
If pop\d=1 Then
dx1=12
dx2=2
dx3=16
Else
dx1=-12
dx2=-2
dx3=-16
End If 

;Lengthens beam if it has not reached full extension otherwise shortens it
If (pop\x2*pop\d)<(pop\xmax*pop\d) 
pop\x2=pop\x2+dx1
pop\x=pop\x+dx2
Else pop\x=pop\x+dx3
End If 

;collision detection between laser and baddie
For bem.blob = Each blob
im_temp=assign_img(bem\im)
col(8)
;Rect pop\x,pop\y,(pop\x2-pop\x)*pop\d,1
Rect pop\x,pop\y,pop\x2,1,-100
If ImageRectOverlap(im_temp,bem\x,bem\y,pop\x2,pop\y,(pop\x-pop\x2),1) Then ; With this line the whold beam is deadly
bem\hp=0
End If 
;bem\hp=bem\hp-1; decreases baddied hit points
;pop\x=pop\xmax; will cause beam to die by trigering deletion later in this function

Next

;Deletes beam once length back To zero. Also shifts the beam colour.
If (pop\x*pop\d)>=(pop\xmax*pop\d) Then 
Delete pop
nbeams=nbeams-1; decreases beam count
lascol=(lascol+1) Mod(5)
Color 255,255,0
End If 
Next
End Function

;Stops x# from exceeding min and max values by ensuring direction of velocity is towards the boundary if object leaves the edge.
;Used to stop things leaving the edge of the screen.
Function confine#(x#,dx#,min,max)
If x#<min And dx#<0 Then dx#=-dx#
If x#>max And dx#>0 Then dx#=-dx#
Return dx#
End Function

Function scanner(x,y,c,s)
col(c)
sx=x/10
sy=y/10
Rect midx+sx,H(1)+sy,s,s
End Function

Function col(c)
Select c
Case 0 
Color 255,0,100
Case 1
Color 255,255,0
Case 2
Color 0,255,0
Case 3
Color 0,255,255
Case 4
Color 100,100,255
Case 5
Color 255,0,255
Case 6
Color 255,0,0
Case 7
Color 255,255,255
Default
Color 255,255,255
End Select
End Function

Function greet()
DrawImage im_asteroid,midx,H(2)
DrawImage im_defender,midx,H(5)
DrawImage im_command,midx,H(8)
Flip 
WaitKey 

End Function 
