import { createClient } from "https://esm.sh/@supabase/supabase-js@2.7.1";

Deno.serve(async (_req) => {
  try {
    console.log("Running wellness check...");
    
    const supabaseUrl = Deno.env.get('SUPABASE_URL') ?? '';
    const supabaseKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? '';
    
    const supabase = createClient(supabaseUrl, supabaseKey);
    
    const { data: profiles, error: fetchError } = await supabase
      .from('profiles')
      .select('id, full_name, notification_enabled');
      
    if (fetchError) throw fetchError;
    
    const notifications: any[] = [];
    
    for (const profile of (profiles || [])) {
      const todayStart = new Date();
      todayStart.setHours(0,0,0,0);
      
      const { data: checkIns } = await supabase
        .from('notifications')
        .select('*')
        .eq('type', 'family')
        .eq('title', 'ডেইলি চেক-ইন')
        .gte('created_at', todayStart.toISOString());
        
      if (!checkIns || checkIns.length === 0) {
        const { data: notif } = await supabase.from('notifications').insert([
          {
            title: 'ডেইলি চেক-ইন',
            message: 'আসসালামু আলাইকুম আম্মু, আপনি কেমন আছেন? পরিবারের সবাইকে জানাতে ভালো থাকার চেক-ইন সম্পন্ন করুন।',
            type: 'family',
            is_read: false,
            created_at: new Date().toISOString()
          }
        ]).select();
        
        if (notif) notifications.push(...notif);
      }
    }

    return new Response(
      JSON.stringify({ 
        success: true, 
        message: `Wellness check completed. Reminders sent: ${notifications.length}`,
        notifications: notifications
      }),
      { headers: { "Content-Type": "application/json" } }
    );
  } catch (error) {
    console.error("Error in wellness-check:", error);
    return new Response(
      JSON.stringify({ error: (error as any).message }),
      { status: 500, headers: { "Content-Type": "application/json" } }
    );
  }
});
