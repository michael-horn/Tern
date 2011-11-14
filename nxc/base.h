
#define STEP_PAUSE  100
#define DEFAULT_POWER  50
#define FULL_POWER  100
#define TURN_TIME 1000

void Forward() 
{
OnFwd(OUT_AC, DEFAULT_POWER  );
Wait(1000);
Off(OUT_AC);
}
void Backward() 
{
OnFwd(OUT_AC, -DEFAULT_POWER  );
Wait(1000);
Off(OUT_AC);
}

void Left()
 {
OnFwd(OUT_C, 45 );
Wait(1500);
Off(OUT_C) ;
Wait(STEP_PAUSE);


}
void Right()
 {
OnFwd(OUT_C, 45 );
Wait(1500);
Off(OUT_C) ;
Wait(STEP_PAUSE);

}
void Whistle()
{
   PlayTone(700, 100);
   Wait(100);
}


void Spin() 
{
OnFwd(OUT_A,  DEFAULT_POWER  );
OnFwd(OUT_C, - DEFAULT_POWER  ); 
   Wait(4000);
   Off(OUT_AC);
   Wait(STEP_PAUSE);
}


void Shake() {
   int i;
   int pa = FULL_POWER;
   int pc = -FULL_POWER;

   for (i = 0; i < 10; i++) {
      OnFwd (OUT_A, pa);
      OnFwd (OUT_C, pc);
      Wait(100);
      pa *= -1;
      pc *= -1;
   }
   Off(OUT_AC);
      Wait(STEP_PAUSE);
}


void Wiggle() {
   int i;
   for (i = 0; i < 5; i++) {
      OnFwd (OUT_A, 0);
      OnFwd (OUT_C, 30);
      Wait(200);
       OnFwd (OUT_A, 30);
       OnFwd (OUT_C, 0);
      Wait(100);
   }
   Off(OUT_A);
   Off(OUT_C);
   Wait(STEP_PAUSE);
}


void Shuffle() {
   int i;
   for (i = 0; i < 4; i++) {
      OnFwd (OUT_AC, DEFAULT_POWER);
      Wait(500);
      OnRev (OUT_AC, -DEFAULT_POWER);
      Wait(500);
   }
   Off(OUT_AC);
   Wait(STEP_PAUSE);
}

void Beep() 
{
   PlayTone(600, 100);

}
void Growl()
{
   PlayTone(500, 100); 
}


void MoonSkill() { 
   Forward();
   Wait(1000);
   Backward();
}

