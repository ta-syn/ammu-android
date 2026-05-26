import { createClient } from "https://esm.sh/@supabase/supabase-js@2.7.1";

Deno.serve(async (_req) => {
  try {
    console.log("Running prayer-notifications check...");
    
    const supabaseUrl = Deno.env.get('SUPABASE_URL') ?? '';
    const supabaseKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? '';
    
    const supabase = createClient(supabaseUrl, supabaseKey);
    
    const { data: profiles, error: fetchError } = await supabase
      .from('profiles')
      .select('*')
      .eq('notification_enabled', true);
      
    if (fetchError) throw fetchError;
    
    const notifications: any[] = [];
    for (const profile of (profiles || [])) {
      const todayStart = new Date();
      todayStart.setHours(0,0,0,0);
      
      const { data: existing } = await supabase
        .from('notifications')
        .select('*')
        .eq('user_id', profile.id)
        .eq('type', 'prayer')
        .eq('title', 'নামাজের সময়')
        .gte('created_at', todayStart.toISOString());
        
      if (!existing || existing.length === 0) {
        const { data: notif } = await supabase.from('notifications').insert([
          {
            user_id: profile.id,
            title: 'নামাজের সময়',
            message: 'আসসালামু আলাইকুম, নামাজের সময় হয়েছে। অনুগ্রহ করে সময়মতো সালাত আদায় করুন।',
            type: 'prayer',
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
        message: "Prayer notifications processed successfully.",
        count: notifications.length,
        notifications: notifications
      }),
      { headers: { "Content-Type": "application/json" } }
    );
  } catch (error) {
    console.error("Error in prayer-notifications:", error);
    return new Response(
      JSON.stringify({ error: (error as any).message }),
      { status: 500, headers: { "Content-Type": "application/json" } }
    );
  }
});
