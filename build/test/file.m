function varargout = uispecgram(varargin)
% UISPECGRAM M-file for uispecgram.fig
%      USAGE: uispecgram(waveform, [spectralobject], [datasource])
%       spectralobject and datasource parameters are optional, and order
%       does not matter.
%
%
%      UISPECGRAM('CALLBACK',hObject,eventData,handles,...) calls the local
%      function named CALLBACK in UISPECGRAM.M with the given input arguments.
%
%      *See GUI Options on GUIDE's Tools menu.  Choose "GUI allows only one
%      instance to run (singleton)".
%
% See also: GUIDE, GUIDATA, GUIHANDLES, spectralobject, spectralobject/specgram2, spectralobject/specgram
%
% 

% Modified by hand 10/22/2005
%
% Edit the above text to modify the response to help uispecgram

% Last Modified by GUIDE v2.5 13-Mar-2010 15:24:21

% Begin initialization code - DO NOT EDIT
gui_Singleton = 1;
gui_State = struct('gui_Name',       mfilename, ...
  'gui_Singleton',  gui_Singleton, ...
  'gui_OpeningFcn', @uispecgram_OpeningFcn, ...
  'gui_OutputFcn',  @uispecgram_OutputFcn, ...
  'gui_LayoutFcn',  [] , ...
  'gui_Callback',   []);
if (nargin > 0) && ischar(varargin{1})
  gui_State.gui_Callback = str2func(varargin{1});
end

if nargout
  [varargout{1:nargout}] = gui_mainfcn(gui_State, varargin{:});
else
  gui_mainfcn(gui_State, varargin{:});
end
% End initialization code - DO NOT EDIT

% --- Executes just before uispecgram is made visible.
function uispecgram_OpeningFcn(hObject, eventdata, handles, varargin)
% This function has no output args, see OutputFcn.
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
% varargin   command line arguments to uispecgram (see VARARGIN)

% Choose default command line output for uispecgram
handles.output = hObject;

% Update handles structure
guidata(hObject, handles);


%celso code
nArgs = numel(varargin);
userData = struct('wave',waveform,'spec',spectralobject,'source',datasource);
for i=1:nArgs
  if isa(varargin{i},'waveform')
    userData.wave = varargin{i};
  elseif isa(varargin{i},'spectralobject')
    userData.spec = varargin{i};
  elseif isa(varargin{i},'datasource')
    userData.source = varargin{i};
  end
end
if (numel(userData.wave)> 1) || isempty(userData.wave(1))
  error('uispecgram must be called with a single waveform object.');
end

set(hObject,'UserData',userData); %spectralobject & waveform
set(handles.menu_xunits,'userdata','Seconds');


% This sets up the initial plot - only do when we are invisible
% so window can get raised using uispecgram.
if strcmp(get(hObject,'Visible'),'off')
  %set pointer to watch
  %P = getptr(gcf);
  %setptr(gcf,'watch')
  
  %draw the waveform
  axes(handles.axes_waveform);
  plot(userData.wave);
  axis tight
  
  %draw the spectrogram
  %   axes(handles.axes_specgram);
  specgram(userData.spec,userData.wave,...
      'axis',handles.axes_specgram,...
      'colorbar','none',...
      'yscale',get(handles.checkbox_logy,'UserData'));
  handles.output = userData.spec;
  guidata(hObject,handles)
  title('');
  colorbar('horiz');
  
  %set the parameters to match ouir input parameter...
  
  % --------------------------------------------------------------------
  % Initialize the slider and edit box UI controls
  dBLimits = get(userData.spec,'dbLims');
  lowerLimit = dBLimits(1); upperLimit = dBLimits(2);
  
  max_dbLim_value = 200;
  min_dbLim_value = 0;
  min_dBLim_step = 1/max_dbLim_value;
  max_dBLim_step = 10/max_dbLim_value;
  
  % ... Set the Lower dBLims slider
  set(handles.minDBLIMS,...
    'value',lowerLimit,...
    'sliderstep',[min_dBLim_step max_dBLim_step],...
    'max',max_dbLim_value,...
    'min',min_dbLim_value);
  set(handles.edit_MIN,'value',lowerLimit,'string',num2str(lowerLimit));
  
  % ... Set the Upper dBLims slider
  set(handles.maxDBLIMS,...
    'value', upperLimit,...
    'sliderstep', [min_dBLim_step max_dBLim_step],...
    'max',max_dbLim_value,...
    'min',min_dbLim_value);
  
  set(handles.edit_MAX,'value',upperLimit,'string',num2str(upperLimit));
  
  % --------------------------------------------------------------------
  %... set the Frequency slider
  max_freq_value = 49;
  min_freq_value = 1;
  min_freq_step = 1 / max_freq_value;
  max_freq_step = 10 / max_freq_value;
  current_max_freq = get(userData.spec,'freqMax');
  set(handles.slider_FreqMax,...
    'value', current_max_freq,...
    'sliderstep',[min_freq_step max_freq_step],...
    'max',max_freq_value,'min',min_freq_value);
  set(handles.edit_FreqMax,...
    'value',current_max_freq,...
    'string', num2str(current_max_freq));
  
  current_nfft = find(get(userData.spec,'NFFT')==[16384 8192 4096 2048 1024 512 256]);
  if isempty(current_nfft), current_nfft = 8; end
  %     switch get(userData.spec,'NFFT')
  %         case 256, current_nfft = 7;
  %         case 512, current_nfft = 6;
  %         case 1024, current_nfft = 5;
  %         case 2048, current_nfft = 4;
  %         case 4096, current_nfft = 3;
  %         case 8192, current_nfft = 2;
  %         case 16384, current_nfft = 1;
  %         otherwise
  %             current_nfft = 8; %custom
  %     end
  set(handles.popupmenu_NFFT,'value',current_nfft);
  
  nfft = get(userData.spec, 'NFFT');
  overlap = get(userData.spec,'OVER');
  freq = get(userData.wave,'Fs');
  dif = round(nfft - overlap);
  percent_overlap = round((nfft - overlap) / nfft * 100);
  overlapMenuItem = 0;
  
  switch percent_overlap
    case 20, overlapMenuItem = 2;
    case 50, overlapMenuItem = 3;
    case 80, overlapMenuItem = 4;
    case 90, overlapMenuItem = 5;
  end
  if (overlapMenuItem == 0)
    % didn't match a percent overlap, see if there's a dif
    switch dif
      case 0, overlapMenuItem = 1;
      case freq, overlapMenuItem = 6;
      case (freq * 2), overlapMenuItem = 7;
      case (freq * 5), overlapMenuItem = 8;
      case (freq * 10), overlapMenuItem = 9;
      case (freq * 30), overlapMenuItem = 10;
      case (freq * 60), overlapMenuItem = 11;
      otherwise
        %unaccounted for overlap.  However, we need to be able to handle
        %it.
        possibleOverlaps = get(handles.popupmenu_OVER,'string');
        possibleOverlaps(12) = {num2str(overlap)};
        set(handles.popupmenu_OVER,'string',possibleOverlaps);
        overlapMenuItem = 12;
    end
  end
  set(handles.popupmenu_OVER,'value',overlapMenuItem);
  if (nargout==1)
    uiwait(handles.specgramfig);
  end
  
  
  %setptr(gcf,P{:}) %set pointer back to normal
end

% UIWAIT makes uispecgram wait for user response (see UIRESUME)
% uiwait(handles.specgramfig);


% --- Outputs from this function are returned to the command line.
function varargout = uispecgram_OutputFcn(hObject, eventdata, handles)
% varargout  cell array for returning output args (see VARARGOUT);
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Get default command line output from handles structure
varargout{1} = handles.output;
uiresume(handles.specgramfig);

% --- Executes on button press in pushbutton1.
function pushbutton1_Callback(hObject, eventdata, handles)
% hObject    handle to pushbutton1 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
axes(handles.axes_specgram);
userData = get(handles.specgramfig,'userdata');


% nfft = get(userData.spec,'NFFT');

% modify the stored spectralobject based on NFFT value from the pop-up menu
NFFT_sel_index = get(handles.popupmenu_NFFT, 'Value');
possibleNFFTvalues = get(handles.popupmenu_NFFT,'string');
nfft = str2double(possibleNFFTvalues(NFFT_sel_index));
userData.spec = set(userData.spec,'NFFT',nfft);

% modify the overlap based upon the overlap value from pop-up menu
userData.spec = set(userData.spec,'FreqMax',get(handles.slider_FreqMax,'value'));
freq = get(userData.wave,'Fs');

switch get(handles.popupmenu_OVER,'value')
  case 1, overlap = 0;
  case 2, overlap = .20 * nfft;
  case 3, overlap = .50 * nfft;
  case 4, overlap = .80 * nfft;
  case 5, overlap = .90 * nfft;
  case 6, overlap = nfft - (freq);
  case 7, overlap = nfft - (freq * 2);
  case 8, overlap = nfft - (freq * 5);
  case 9, overlap = nfft - (freq * 10);
  case 10, overlap = nfft - (freq * 30);
  case 11, overlap = nfft - (freq * 60);
  case 12, allOverlapStrings = get(handles.popupmenu_OVER,'string');
    overlap = str2double(allOverlapStrings(12));
end

pointerShape = getptr(gcf);
setptr(gcf,'watch')

userData.spec = set(userData.spec,'over',overlap);
dbls = [get(handles.minDBLIMS,'value'), get(handles.maxDBLIMS,'value')];
if dbls(1) > dbls(2)
  set(handles.minDBLIMS,'value',dbls(2));
  set(handles.edit_MIN,'value',dbls(2),'string',num2str(dbls(2)));
  set(handles.maxDBLIMS,'value',dbls(1));
  set(handles.edit_MAX,'value',dbls(1),'string',num2str(dbls(1)));
end;
userData.spec = set(userData.spec,'dblims', sort(dbls));

axes(handles.axes_specgram);

specgram(userData.spec,userData.wave,...
    'xunit',get(handles.menu_xunits,'userdata'),...
    'axis', handles.axes_specgram,...
    'colorbar','none',...
    'yscale',get(handles.checkbox_logy,'UserData'));
%specgram(userData.spec,userData.wave);
%colorbar('horiz');
title('');
set(handles.specgramfig,'userdata',userData);
handles.output = userData.spec;
guidata(hObject,handles);

set(gcf,pointerShape{:}); %set pointer back to what it was before...

% --------------------------------------------------------------------
function FileMenu_Callback(hObject, eventdata, handles)
% hObject    handle to FileMenu (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)


% --------------------------------------------------------------------
function OpenMenuItem_Callback(hObject, eventdata, handles)
% hObject    handle to OpenMenuItem (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
file = uigetfile('*.fig');
if ~isequal(file, 0)
  open(file);
end

% --------------------------------------------------------------------
function PrintMenuItem_Callback(hObject, eventdata, handles)
% hObject    handle to PrintMenuItem (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
printdlg(handles.specgramfig)

% --------------------------------------------------------------------
function CloseMenuItem_Callback(hObject, eventdata, handles)
% hObject    handle to CloseMenuItem (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
selection = questdlg(['Close ' get(handles.specgramfig,'Name') '?'],...
  ['Close ' get(handles.specgramfig,'Name') '...'],...
  'Yes','No','Yes');
if strcmp(selection,'No')
  return;
end

delete(handles.specgramfig)


% --- Executes during object creation, after setting all properties.
function popupmenu_NFFT_CreateFcn(hObject, eventdata, handles)
% hObject    handle to popupmenu3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: popupmenu controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc
  set(hObject,'BackgroundColor','white');
else
  set(hObject,'BackgroundColor',get(0,'defaultUicontrolBackgroundColor'));
end

set(hObject, 'String', {'16384', '8192', '4096', '2048', '1024', '512', '256'});

% --- Executes on selection change in popupmenu3.
function popupmenu_NFFT_Callback(hObject, eventdata, handles)
% hObject    handle to popupmenu3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: contents = get(hObject,'String') returns popupmenu3 contents as cell array
%        contents{get(hObject,'Value')} returns selected item from popupmenu3

autoUpdatePlot(eventdata,handles);

% --- Executes during object creation, after setting all properties.
function minDBLIMS_CreateFcn(hObject, eventdata, handles)
% hObject    handle to minDBLIMS (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: slider controls usually have a light gray background, change
%       'usewhitebg' to 0 to use default.  See ISPC and COMPUTER.
usewhitebg = 1;
if usewhitebg
  set(hObject,'BackgroundColor',[.9 .9 .9]);
else
  set(hObject,'BackgroundColor',get(0,'defaultUicontrolBackgroundColor'));
end


% --- Executes on slider movement.
function minDBLIMS_Callback(hObject, eventdata, handles)
% hObject    handle to minDBLIMS (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'Value') returns position of slider
%        get(hObject,'Min') and get(hObject,'Max') to determine range of slider
set(hObject,'Value',round(get(hObject,'Value')));
set(handles.edit_MIN,'string',num2str(get(hObject,'Value')),'value',get(hObject,'Value'));

autoUpdatePlot(eventdata,handles);

% --- Executes during object creation, after setting all properties.
function maxDBLIMS_CreateFcn(hObject, eventdata, handles)
% hObject    handle to maxDBLIMS (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: slider controls usually have a light gray background, change
%       'usewhitebg' to 0 to use default.  See ISPC and COMPUTER.
%usewhitebg = 1;
if ispc
  set(hObject,'BackgroundColor',[.9 .9 .9]);
else
  set(hObject,'BackgroundColor',get(0,'defaultUicontrolBackgroundColor'));
end


% --- Executes on slider movement.
function maxDBLIMS_Callback(hObject, eventdata, handles)
% hObject    handle to maxDBLIMS (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'Value') returns position of slider
%        get(hObject,'Min') and get(hObject,'Max') to determine range of slider

set(hObject,'Value',round(get(hObject,'Value')));
set(handles.edit_MAX,'string',num2str(get(hObject,'Value')),'value',get(hObject,'Value'));

autoUpdatePlot(eventdata,handles);

% --- Executes during object creation, after setting all properties.
function popupmenu_OVER_CreateFcn(hObject, eventdata, handles)
% hObject    handle to popupmenu_OVER (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: popupmenu controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc
  set(hObject,'BackgroundColor','white');
else
  set(hObject,'BackgroundColor',get(0,'defaultUicontrolBackgroundColor'));
end


% --- Executes on selection change in popupmenu_OVER.
function popupmenu_OVER_Callback(hObject, eventdata, handles)
% hObject    handle to popupmenu_OVER (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: contents = get(hObject,'String') returns popupmenu_OVER contents as cell array
%        contents{get(hObject,'Value')} returns selected item from popupmenu_OVER

autoUpdatePlot(eventdata,handles);

% --- Executes during object creation, after setting all properties.
function edit_MAX_CreateFcn(hObject, eventdata, handles)
% hObject    handle to edit_MAX (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc
  set(hObject,'BackgroundColor','white');
else
  set(hObject,'BackgroundColor',get(0,'defaultUicontrolBackgroundColor'));
end



function edit_MAX_Callback(hObject, eventdata, handles)
% hObject    handle to edit_MAX (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of edit_MAX as text
%        str2double(get(hObject,'String')) returns contents of edit_MAX as a double


% --- Executes during object creation, after setting all properties.
function edit_MIN_CreateFcn(hObject, eventdata, handles)
% hObject    handle to edit_MIN (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc
  set(hObject,'BackgroundColor','white');
else
  set(hObject,'BackgroundColor',get(0,'defaultUicontrolBackgroundColor'));
end



function edit_MIN_Callback(hObject, eventdata, handles)
% hObject    handle to edit_MIN (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of edit_MIN as text
%        str2double(get(hObject,'String')) returns contents of edit_MIN as a double


% --- Executes during object creation, after setting all properties.
function slider_FreqMax_CreateFcn(hObject, eventdata, handles)
% hObject    handle to slider_FreqMax (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: slider controls usually have a light gray background, change
%       'usewhitebg' to 0 to use default.  See ISPC and COMPUTER.
usewhitebg = 1;
if usewhitebg
  set(hObject,'BackgroundColor',[.9 .9 .9]);
else
  set(hObject,'BackgroundColor',get(0,'defaultUicontrolBackgroundColor'));
end


% --- Executes on slider movement.
function slider_FreqMax_Callback(hObject, eventdata, handles)
% hObject    handle to slider_FreqMax (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'Value') returns position of slider
%        get(hObject,'Min') and get(hObject,'Max') to determine range of slider

set(hObject,'Value',round(get(hObject,'Value')));
set(handles.edit_FreqMax,'string',num2str(get(hObject,'Value')),'value',get(hObject,'Value'));

autoUpdatePlot(eventdata,handles);

% --- Executes during object creation, after setting all properties.
function edit_FreqMax_CreateFcn(hObject, eventdata, handles)
% hObject    handle to edit_FreqMax (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc
  set(hObject,'BackgroundColor','white');
else
  set(hObject,'BackgroundColor',get(0,'defaultUicontrolBackgroundColor'));
end



function edit_FreqMax_Callback(hObject, eventdata, handles)
% hObject    handle to edit_FreqMax (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of edit_FreqMax as text
%        str2double(get(hObject,'String')) returns contents of edit_FreqMax as a double


% --- Executes on button press in checkbox_AutoUpdate.
function checkbox_AutoUpdate_Callback(hObject, eventdata, handles)
% hObject    handle to checkbox_AutoUpdate (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of checkbox_AutoUpdate
autoUpdatePlot(eventdata,handles);

% --------------------------------------------------------------------
function Specgram2_Callback(hObject, eventdata, handles)
% hObject    handle to Specgram2 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
%Generates a specgram2 figure, suitable for printing and more...

h = figure
userData = get(handles.specgramfig,'userdata');
userData.wave
userData.spec
figure(h);
specgram2(userData.spec,userData.wave);
drawnow;


% --------------------------------------------------------------------
function menu_waveform_Callback(hObject, eventdata, handles)
% hObject    handle to menu_waveform (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)


% --------------------------------------------------------------------
function menuitem_changetimes_Callback(hObject, eventdata, handles)
% hObject    handle to menuitem_changetimes (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% get old waveform for defaults...
userData = get(handles.specgramfig,'userdata');

if strcmpi(get(userData.source,'type'),'none')
  warning('No data source exists');
  set(hObject,'enable','off');
  set(handles.menuitem_changestation,'enable','off');
end

stt = get(userData.wave,'start');
ent = get(userData.wave,'end');

anS = inputdlg('Start time?','Waveform Starting time',1,{[datestr(stt,23) ' ' datestr(stt,13)]});
%Answer = INPUTDLG(Prompt,Title,LineNo,DefAns
if datenum(anS) > ent, ent = datenum(anS), end;  %default to new start time.
anE = inputdlg('End time?','Waveform Ending time',1,{[datestr(ent,23) ' ' datestr(ent,13)]});

try
neww = waveform(userData.source,get(userData.wave,'scnlobject'),anS{1},anE{1});
catch
    disp('unable to access a waveform.  have a blank one instead');
end
if ~exist('neww','var') || isempty(neww)
    neww = set(userData.wave,'start',anS{1},'data',[0 0 0]);
    % return
end
if exist('neww','var')
  userData.wave = neww;
  set(handles.specgramfig,'userdata',userData);
  pointerShape = getptr(gcf);
  setptr(gcf,'watch') % change cursor to watch (courtesy)
  axes(handles.axes_waveform);
  plot(userData.wave,'xunit',get(handles.menu_xunits,'userdata'));
  axis tight
  set(gcf,pointerShape{:})
  uispecgram('pushbutton1_Callback',handles.pushbutton1,eventdata,handles) %plot the spectra for the new waveform
end

% --------------------------------------------------------------------
function menuitem_changestation_Callback(hObject, eventdata, handles)
% hObject    handle to menuitem_changestation (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
userData = get(handles.specgramfig,'userdata');
if isunassigned(userData.source)
    % no data source has been chosen
    vnames = evalin('base','whos'); %get list of variables from main workspace
    vnames = vnames(strcmp({vnames.class},'datasource')); %keep datasource
    [selection, ok] = listdlg(...
        'PromptString',...
        'Choose a datasource variable, or the *new datasource*',...
        'listsize',[220 300],...
        'liststring', [{vnames.name},{'*new datasource*'}] );
    if ~ok,
        return
    end
    if selection > numel(vnames)
        %create a datasource from scratch
        answer = inputdlg('Type the datasource creation command.   ',...
            'Create datasource',1,{'datasource(''file'',''filename'')'},'on');
        if isempty(answer)
            return;
        end
        try
            userData.source = eval(answer);        
            set(handles.specgramfig,'userdata',userData);
        catch
            errordlg(['Unable to create the datasource object'],'Problem creating datasource');
        end
    else
        userData.source = evalin('base',vnames(selection).name);
        set(handles.specgramfig,'userdata',userData);
    end
    
    
end
scnl = uiscnlobject(get(userData.wave,'scnlobject'))
pointerShape = getptr(gcf);
neww = waveform(userData.source, scnl, ...
    get(userData.wave,'start'),...
    get(userData.wave,'end'));
if numel(neww) == 0
  errordlg('No waveform was found','invalid station', 'modal');
else
  set(gcf,pointerShape{:})
  if isa(neww,'waveform')
    userData.wave = neww;
    set(handles.specgramfig,'userdata',userData); %make the new waveform available
    setptr(gcf,'watch') % change cursor to watch (courtesy)
    axes(handles.axes_waveform);
    plot(userData.wave,'xunit',get(handles.menu_xunits,'userdata'));
    axis tight
    set(gcf,pointerShape{:})
    uispecgram('pushbutton1_Callback',handles.pushbutton1,eventdata,handles) %plot the spectra for the new waveform
  end
end


%%%HELPER FUNCTION %%%

function create_spectralobject_in_workspace(handles)
prompt = 'Name of variable to create?';
name = 'create a spectralobject variable in the workspace';
defaultanswer = {'s'};
numlines= 1;
options.Resize='on';
options.WindowStyle='modal';
options.Interpreter='none';
answer = inputdlg(prompt,name,numlines,defaultanswer,options);
userData = get(handles.specgramfig,'userdata');
assignin('base',answer{1},userData.spec);


% --------------------------------------------------------------------
function specgram_Callback(hObject, eventdata, handles)
% hObject    handle to specgram (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
h = figure;
userData = get(handles.specgramfig,'userdata');
figure(h);
specgram(userData.spec,userData.wave,...
    'xunit',get(handles.menu_xunits,'UserData'),...
    'axis',gca,...
    'yscale',get(handles.checkbox_logy,'UserData')...
    );
drawnow;

% --------------------------------------------------------------------
function createSpectralobjectMenuItem_Callback(hObject, eventdata, handles)
% hObject    handle to createSpectralobjectMenuItem (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
try
  create_spectralobject_in_workspace(handles);
catch
  error('Unable to save spectralobject to workspace');
end


% --------------------------------------------------------------------
function spectralobjectMenu_Callback(hObject, eventdata, handles)
% hObject    handle to spectralobjectMenu (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)


% --------------------------------------------------------------------
function plotMenu_Callback(hObject, eventdata, handles)
% hObject    handle to plotMenu (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)


% --------------------------------------------------------------------
function menu_xunits_Callback(hObject, eventdata, handles)
% hObject    handle to menu_xunits (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)


function menu_setxunit_Callback(hObject, eventdata, handles)
% hObject    handle to menu_date (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)



set(handles.menu_xunits,'userdata',get(hObject,'Label'));
set(handles.menu_seconds,'checked','off');
set(handles.menu_minutes,'checked','off');
set(handles.menu_hours,'checked','off');
set(handles.menu_date,'checked','off');
set(handles.menu_doy,'checked','off');

set(hObject,'checked','on');
replot_waveform(handles);
uispecgram('pushbutton1_Callback',handles.pushbutton1,eventdata,handles) %plot the spectra for the new waveform

function replot_waveform(handles)
axes(handles.axes_waveform)
userData = get(handles.specgramfig,'userdata');
xunits = get(handles.menu_xunits,'userdata');
plot(userData.wave,'xunit',xunits);
axis tight


% --- Executes on button press in checkbox_logy.
function checkbox_logy_Callback(hObject, eventdata, handles)
% hObject    handle to checkbox_logy (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of checkbox_logy
if get(hObject,'value')
    set(hObject,'UserData','log');
else
    set(hObject,'UserData','normal');
end

autoUpdatePlot(eventdata,handles);

function autoUpdatePlot(eventdata,handles)
% Automatically update the plot IF AutoUpdate is checked
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
if get(handles.checkbox_AutoUpdate,'value')
  uispecgram('pushbutton1_Callback',handles.pushbutton1,eventdata,handles)
end


% --- Executes during object creation, after setting all properties.
function checkbox_logy_CreateFcn(hObject, eventdata, handles)
% hObject    handle to checkbox_logy (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Check to see if we have to ability to create log spectrograms, if not,
% then disable the log checkbox.

if exist('uimagesc.m','file') ~= 0
    set(hObject,'Enable','on');
else
    set(hObject,'Enable','off');
end


% --------------------------------------------------------------------
function menu_about_Callback(hObject, eventdata, handles)
% hObject    handle to menu_about (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
h = helpdlg('Version r228, by Celso G. Reyes, University of Alaska Fairbanks http://www.mathworks.com/matlabcentral/fileexchange/authors/53809','About uispecgram');