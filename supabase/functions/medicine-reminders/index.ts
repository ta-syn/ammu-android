import { createClient } from "https://esm.sh/@supabase/supabase-js@2.7.1";

Deno.serve(async (_req) => {
  try {
    console.log("Checking medicine reminders...");
    
    const supabaseUrl = Deno.env.get('SUPABASE_URL') ?? '';
    const supabaseKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? '';
    
    const supabase = createClient(supabaseUrl, supabaseKey);
    
    const { data: medicines, error: fetchError } = await supabase
      .from('medicines')
      .select('*')
      .eq('is_active', true);
      
    if (fetchError) throw fetchError;
    
    const now = new Date();
    // Convert current UTC time to Bangladesh Standard Time (BST, UTC+6)
    const bstTime = new Date(now.getTime() + (6 * 60 * 60 * 1000));
    const currentHour = bstTime.getUTCHours();
    const currentMin = bstTime.getUTCMinutes();
    
    const triggers: any[] = [];
    
    for (const med of (medicines || [])) {
      let times: string[] = [];
      try {
        times = JSON.parse(med.times_json || "[]");
      } catch {
        continue;
      }
      
      for (const timeStr of times) {
        const parts = timeStr.split(":");
        if (parts.length !== 2) continue;
        const h = parseInt(parts[0]);
        const m = parseInt(parts[1]);
        
        // Match exact hour and minute in Bangladesh Time (BST)
        if (h === currentHour && m === currentMin) {
          triggers.push(med);
          
          await supabase.from('medicine_logs').insert([
            {
              user_id: med.user_id,
              medicine_id: med.id,
              scheduled_at: now.toISOString(),
              status: 'upcoming'
            }
          ]);
        }
      }
    }

    return new Response(
      JSON.stringify({ 
        success: true, 
        message: `Processed ${medicines?.length || 0} medicines. Reminders triggered: ${triggers.length}`,
        triggered: triggers
      }),
      { headers: { "Content-Type": "application/json" } }
    );
  } catch (error) {
    console.error("Error in medicine-reminders:", error);
    return new Response(
      JSON.stringify({ error: (error as any).message }),
      { status: 500, headers: { "Content-Type": "application/json" } }
    );
  }
});
