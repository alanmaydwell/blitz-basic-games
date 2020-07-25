;Asteroid Defender Command
;A game by Alan Maydwell
;Started 8/11/02

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
Global im_ship_r=LoadImage("data\santa_a.bmp")
Global im_ship_l=LoadImage("data\santa_b.bmp")
Global im_ship_s=LoadImage("data\santa_sm.bmp")

Global im_city=LoadImage("data\singers1.bmp")

Global im_bigast1=LoadImage("data\pudding_b1.bmp")

Global im_midast1=LoadImage("data\pudding_m1.bmp")

Global im_smast1=LoadImage("data\pudding_s1.bmp")


Global im_asteroid=LoadImage("data\asteroid.bmp")
Global im_defender=LoadImage("data\defender1.bmp")
Global im_command=LoadImage("data\command.bmp")

Global im_back=LoadImage("data\treeback.bmp")


;Load Sound
Global sn_laser=LoadSound("data\def_laser.wav")
Global sn_exp=LoadSound("data\def_bip.wav")
Global sn_daffy=LoadSound("data\daffy.wav")
Global sn_shipexp=LoadSound("data\crash3.wav")
Global sn_siren=LoadSound("data\airaid.wav")



;Games parameters
Global startlives=3; Starting number of lives
Global lives=3     ; current number of lives
Global score=0     ; Score
Global level=1     ; Level
Global cook=0         ; Fudge factor to ensure that new baddies  
				   ; Are not created too close to the cities.
Global count=0     ;Prgram loopcounter    
				
				
;High Score Table Settings
Global ns=4; 	Number of entries (-1) to be stored in the High Score Table. 
Dim hst(ns);	stores scores
Dim hsnames$(ns);Stores names

 
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
Global status=1;  1 - Ship alive and game running: 0 Ship dead and level running: -1 -Ship dead and level over.

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

;Create paths
Global max=720
Dim path(max,1)
For loop=0 To max
x=loop*width/max
y=loop*height/max
path(loop,0)=40*Sin(90+loop*2)
path(loop,1)=40*Cos(90+loop*2)
Next 

;For loop=0 To max
;Plot midx+path(loop,0),midy+path(loop,1)
;Next
;WaitKey 


;City parameters
Global ncity=0;number of cities
Type town
Field x ;x
Field y ;y
Field s ;Status (alive/dead)
End Type
Global citywidth=ImageWidth(im_city)    ; width of ship image
Global cityheight=ImageHeight(im_city)	 ; height of ship image

;**********************
;*Explosion Parameters*
;**********************
Type expl
Field x
Field y
Field dx
Field dy
Field e ;lifetime of explosion
Field s	;Particle Size
Field c ;explosion colour
End Type



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


;Stary decorative things parameters
Type spid
Field x
Field y
Field l ;Lifetime
Field c ;Colour
End Type 

;enable double buffering
SetBuffer BackBuffer()
sethighscore(ns)

Repeat; Outer loop (1)
greet()
newplanet=True 

;GAME STARTS HERE
Repeat; Game in progress loop. (2)

If newplanet=True Then
set_cities()
set_skyline()
newplanet=False 
End If 

set_ship()
;If loop has started becuase new level started then generate new baddies.
If status =1 Then 
set_level(level); Sets up baddies at start of level
set_spids(32)     ;Sets up some new stary dots
PlaySound(sn_siren)
Else status=1 
End If

;loop until ESC hit.
count=0;reset loopcounter
Repeat; Level in progress loop. (3) 
If KeyDown(1) End 
Cls

;If ship alive draw it and make controlls active.
;If ship is dead update countdown to rebirth or game over.
;Also generates addition explosions if ship is dead

update_spids()

If status =1 Then 
countdown=0
update_ship()
control_ship()
Else
countdown=countdown+1
If countdown Mod(10)=1 Then Exp(player_x,player_y,128,32,Rnd(1,2),Rnd(0,7))
player_y=player_y+1
If countdown >200 Then status=-1;Trigger rebirth/game over when countdown reached. 
End If 

update_laser()
update_skyline()
update_cities()
update_baddies()
update_exp()
display()
 
Flip 
count=count+1;increment loopcounter
If count Mod(15)=1 And player_dx<>0 Then player_dx=player_dx-(1*player_dx>0)+(1*player_dx<0); Sort of friction to slow down ship.
  
Until status=-1 Or nbem=0 Or KeyDown(1); End of level inprogress loop z.(3)
 									   ; (Stops if ship dead or all baddies destroyed)

If KeyDown(1) End 
If status<>-1 levelover();Starts new level if ship not dead 

Until lives<1 Or KeyDown(1);End of game in progress loop.(2)
                           ;(Game over when all ships gone).
If KeyDown(1) End 
Delay 1000
FlushKeys; (clear keyboard buffer)
gameover() 
update_highscores(score)
clear(1); Clears all data types
FlushKeys; (clear keyboard buffer)
Until KeyDown(1) 
End

;*********************************************************************
;*** FUNCTIONS START HERE ********************************************
;*********************************************************************

Function set_baddie(x,y,im)
bem.blob=New blob
bem\x=x
bem\y=y
bem\dx=2+(level/2)
If bem\dx>8 Then bem\dx=8
If bem\dx=0 Then bem\dx=1
k=Rnd(1,2)
If k=1 
bem\dy=-1 
Else bem\dy=1
End If 
bem\im=im
bem\hp=1 ; Hit point(s)
End Function


Function set_cities()
For loop=1 To 6
city.town=New town
city\x=-300-citywidth+(loop*100)
city\y=H(19)-(cityheight/2)+9
Next
cook=city\x ;  Tracks approx location of cities 
             ; fudge factor used to ensure that new asteroids are not created too close to the cities.
End Function

Function set_level(n)
;Set up baddies for start of level
For loop=1 To n+2
x=cook+1600-loop*80; cook is a fudge factor used to track approx location of cities. 
;y=-loop*5
im=1
set_baddie(x,y,im)
Next
End Function 

Function set_ship()
Color 255,255,255
Rect 0,0,width,height 
Flip 
player_x=V(2)
player_y=H(14)
player_dx=0
player_dy=0

;Moves all baddies above the top of the screen to ensure 
;there will not be an imediate collision between the new shit and a baddy.
For bem.blob = Each blob
If bem\y>-100 Then bem\y=bem\y-height
Next 

End Function 

;Set up snow
Function set_spids(n)
For loop=1 To n
dot.spid=New spid
dot\x=Rnd(0,width)
dot\y=Rnd(H(3),H(13))
dot\l=Rnd(50)+50
dot\c=7
Next 
End Function 

;Creates Random Hilly Skyline
;- direction is a mirror image of the + direction
; this is a lazy way to ensure there is no disconinuity at the wrap point.
Function set_skyline()
y=H(19)
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
If loop<300 Then y=H(19); Forces flat area for cities to be placed
Next
End Function 

Function update_baddies()
nbem=0;Set baddie counter to 0
For bem.blob = Each blob
nbem=nbem+1; count the number of baddies

;Sort of gravity. 
If count Mod(20)=1 And bem\y>0 Then bem\dy=bem\dy+1
If bem\dy=0 Then bem\dy=1

;Causes badies to home in on ship if all cities destroyed
If ncity=0
d=10+Rnd(0,2)
k=Rnd(-100,100)
If bem\x>player_x+k Then 
bem\dx=-d
Else bem\dx=d
End If
End If 


bem\x=bem\x+bem\dx
bem\y=bem\y+bem\dy

;Stops baddies from leaving top or bottom of screen
bem\dy=confine#(bem\y,bem\dy,-100,height)

;updates position relative to ship based on ship's x movement.
bem\x=move_rel(bem\x)
im_temp=assign_img(bem\im)
DrawImage im_temp,bem\x,bem\y
scanner(bem\x,bem\y,3,2)

;collision detection between baddie and ship if ship is active
If status=1 And ImagesCollide(im_temp,bem\x,bem\y,0,im_ship_l,player_x,player_y,0)
Exp(player_x,player_y,128,128,1,Rnd(0,7))
PlaySound(sn_shipexp)
bem\hp=0
lives=lives-1
status=0; Flag set to leave level running but deacitivate controlls and stop displaying ship.
End If 

;collision detection between baddie And city
For city.town = Each town
If ImagesCollide(im_temp,bem\x,bem\y,0,im_city,city\x,city\y,0)
PlaySound(sn_daffy)
Exp(city\x,city\y,64,64,2,0)
Exp(city\x,city\y,64,32,1,7)
bem\hp=0
Delete city 
End If 
Next 


If bem\hp=0 Then 
Delete bem
End If 
Next
End Function

;Selects appropriate image for each baddie
Function assign_img(n)
Select n
Case 1; Big Pud (type 1)
im_temp=im_bigast1
Case 2; Medium Pud (type 2)
im_temp=im_midast1
Case 3; SmallPud (type 3)
im_temp=im_smast1
Default 
im_temp=im_bigast1
End Select
Return im_temp
End Function  


Function update_cities()
ncity=0
For city.town = Each town
ncity=ncity+1
;updates position relative to ship based on ship's x movement.
city\x=move_rel(city\x)
DrawImage im_city,city\x,city\y
scanner(city\x,city\y,4,3)
Next
cook=move_rel(cook); Updates cook to take scrolling movement into account.
             ;  cook  is fudge factor that tracks approx x coordinate of cities.
             ; Used To ensure that New asteroids are Not created too close To the cities.
End Function

;Update snow
Function update_spids()
k=player_dx/2
d=1+Abs(k)
For dot.spid=Each spid
col(dot\c)
;dot\x=move_rel(dot\x)
dot\y=dot\y+1
dot\x=dot\x+k
Rect dot\x,dot\y,d,1
dot\l=dot\l-1
If dot\l=0 Then Delete dot:set_spids(1)
Next
End Function 

Function update_skyline()
col(7)
For hill.point = Each point
hill\x=move_rel(hill\x)
Rect hill\x,hill\y,2,2
If hill\s=True Then scanner(hill\x,hill\y,7,1)
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
scanner(player_x,player_y,6,3)
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
If (KeyHit(28) Or JoyHit(1)) And nbeams<maxbeams Then shoot_laser(player_x+(player_f*50),player_y-4,480,player_dx)
End Function 


;***********************
;*** Laser Functions ***
;***********************

;Initialises new beam
Function shoot_laser(x,y,l,v)
PlaySound(sn_laser)
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

;Updates beam and collision detection between beam and baddie
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

;Collision detection between laser and baddie

;collision detection between laser and baddie
For bem.blob = Each blob
dead=False
im_temp=assign_img(bem\im)
If  pop\d=-1 And ImageRectOverlap(im_temp,bem\x,bem\y,pop\x2,pop\y,(pop\x-pop\x2),1) Then ; With this line the whold beam is deadly
dead=True
End If
If pop\d=1 And ImageRectOverlap(im_temp,bem\x,bem\y,pop\x,pop\y,(pop\x2-pop\x),1)
dead=True
End If  

If dead=True 
PlaySound(sn_exp)
pop\x=pop\xmax; will cause beam to die by trigering deletion later in this function
Exp(bem\x,bem\y,16,32,1,pop\c)
Exp(bem\x,bem\y,16,32,1,7)
bem\hp=0
score=score+1

;Spawns smaller asteroids if big or medium asteroid hit.
If bem\im=1 
For loop=1 To 3
set_baddie(bem\x,bem\y+Rnd(1,30)-15,2)
Next 
End If 
If bem\im=2 Then 
For loop=1 To 2
set_baddie(bem\x,bem\y+Rnd(1,20)-10,3)
Next 
End If 

End If 
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


;Creates explosion centred on x,y made up of n particles 
;that will Last For l loops and are of size s and have colour index expcol.
Function Exp(x,y,n,l,s,expcol)
For loop=1 To n
bop.expl=New expl
bop\s=s
bop\e=l
bop\x=x
bop\y=y
bop\c=expcol
Repeat 
bop\dx=Rnd(-3,3)
bop\dy=Rnd(-3,3)
Until bop\dx<>0 Or bop\dy<>0
Next 
End Function 

;Updates explosion
Function update_exp() 
For bop.expl = Each expl

;Selects colour 
col(bop\c)
;plots particles and expands to new locations
Rect bop\x,bop\y,bop\s,bop\s
;Rect bop\x-bop\dx,bop\y-bop\dy,bop\s,bop\s
Rect bop\x-(2*bop\dx),bop\y-(2*bop\dy),bop\s,bop\s

bop\x=bop\x+bop\dx
bop\y=bop\y+bop\dy
bop\e=bop\e-1
If bop\e<0 Then Delete bop 
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

;Displays end of level message
Function levelover()
player_dx=0
FlushKeys
bonus=0
For loop=1 To ncity
Cls
update_skyline
col(7)
For n=1 To loop 
DrawImage im_city,n*90,H(12)
Next
bonus=bonus+10
score=score+10
display
Color 0,200,255
Text midx,H(14),"City Bonus "+bonus,1,1
Text midx,midy,"Level "+level+" Complete",1,1
Delay 300
Flip
Next 

clear(0) 
Delay 2500

level=level+1
status=1
End Function

Function gameover()
Cls 
Text midx,midy,"GAME OVER",1,1
Flip 
Delay 2000
End Function


;clears data types 
Function clear(n)

;baddies
For bem.blob = Each blob
Delete bem
Next
nbem=0

;Laser beam
For pop.laser = Each laser
Delete pop
Next
nbeams=0

;Explosion
For bop.expl = Each expl
Delete bop
Next

;spids
For dot.spid=Each spid
Delete dot
Next 

If n=1
;Skyline
For hill.point = Each point
Delete hill
Next
;Cities 
For city.town = Each town
Delete city
Next
ncity=0 
End If

End Function 

;Shows number of lives, score etc
Function display()
;show remaining lives
For loop=2 To lives
DrawImage im_ship_s,loop*26,H(1)
Next
;Plot score
Text V(17),H(1),score
Text V(17),H(2),nbem

End Function


Function greet()
counter=0
show=1
set_spids(128);Generate snow
Repeat
Cls
TileImage im_back
Color 120,0,0
update_spids();Update snow
Text midx,20,"Oh No! It's another game by Alan Maydwell.",1,1 
DrawImage im_defender,midx,H(5)
DrawImage im_command,midx,H(8)-20

;Shows either the highscores or the controlls
If show=1 Then
display_info()
Else
display_highscores(ns) 
End If
c=(counter*.1) Mod(7):col(c)
Text midx,H(18),"PRESS SPACE OR FIRE TO START",1,1
Text midx,H(19),"Press Esc to quit",1,1
Flip
counter=counter+1
If counter=300 Then counter=0:show=show*-1 
Until KeyDown(57) Or KeyDown(1) Or GetJoy()>0
FlushKeys; (clear keyboard buffer)
clear(1) ;Wipes all data types 
lives=startlives
level=1
score=0
status=1 
End Function

Function display_info()
Color 255,255,255
Text midx,H(9),"CONTROLS",1,1
Color 0,255,255
Text midx,H(10),"A - up",1,1
Text midx,H(11),"Z - down",1,1
Text midx,H(12),"Right-shift - thrust",1,1
Text midx,H(13),"Return - fire",1,1
Text midx,H(14),"Space - reverse",1,1
Color 0,155,255
Text midx,H(15),"or use a joystick",1,1
Text midx,H(16),"(two firebuttons recommended)",1,1
End Function 


;Displays High Score Table
Function display_highscores(ns)
Color 255,255,255
Text midx,H(10),"HIGH SCORES",1,1
For loop=0 To ns
col(loop)
Text v(8),H(loop+11),hst(loop)+"   "+hsnames$(loop),0,1
Next
End Function

;Sets initial dummy values for high score table
Function sethighscore(ns)
For loop=0 To ns
hst(loop)=0
hsnames$(loop)="Ho He Ha!"
Next
End Function

;Places new score in High Score Table if value is high enough
Function update_highscores(score)

;Checks high score table to see if the new score should be included.
;If new score is to be included its position in table is stored in np.
;-1 value of np is used to signify that score should not be added because it is too low.

np=-1 
pcount=0
Repeat
If score>=hst(pcount) Then np=pcount
pcount=pcount+1 
Until np<>-1 Or pcount>ns


;If new score to be inclued, existing entries from bottom of table to np are moved down one place.
;Except when new score is to be added to the final position (not necessary in such a case).
If np<>-1 And  np<>ns Then 
For loop=ns To np+1 Step -1
hst(loop)=hst(loop-1)
hsnames$(loop)=hsnames$(loop-1)
Next
End If 

If np<>-1 Then 
;adds new score to high score table at position np. 
hst(np)=score
;requests player's name name and stores it in table.
hsnames$(np)=inputname$(np)
End If
End Function


Function inputname$(np)
a$=""
Color 255,255,255
Repeat 
Cls 

;Generates Random Explosions for background
x=Rnd(0,width):y=Rnd(0,height):s=Rnd(1,6)
Exp(x,y,20,50,s,expcol)      ; create explosion
expcol=(expcol+1) Mod(6)             ; Advances the explosion colour index
Exp(x+6,y-6,20,50,s,expcol)  ; create explosion
expcol=(expcol+1) Mod(6)             ; Advances the explosion colour index
update_exp
Color 0,50,100:Rect V(6),H(1),250,230,1 

Color 255,0,100
Text midx,H(2),"Congratulations!",1,1
Color 255,200,0
Text midx,H(3),"Your score of "+score,1,1
Color 220,220,0
Text midx,H(4),"has placed you at position "+(np+1),1,1
Color 0,255,100
Text midx,H(5),"in the High Score Table",1,1
Color 0,140,255
Text midx,H(8),"Please type your name:",1,1

; Checks for keypress and adds corresponding key to the string 
; if it is not blank or backspace or return and string has not 
; reached maximum value..
temp=GetKey()
If temp>0 And temp<>13 And temp<>8 And Len(a$)<17 Then a$=a$+Chr$(temp)

;Deletes character from string if Backspace pressed.
If temp=8 And Len(a$)>0 Then 
l=Len(a$)-1
a$=Left$(a$,l)End If

Color 255,255,255 
Text midx,H(9),a$,1,1 

Flip
If KeyDown(1) End;quit if escape pressed
Until temp=13; Ends when return key pressed

If a$="" Then a$="anon"
Return a$
End Function 